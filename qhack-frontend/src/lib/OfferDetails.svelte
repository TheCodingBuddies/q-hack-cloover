<script lang="ts">
  import { onMount } from 'svelte';
  import { fade, slide } from 'svelte/transition';
  import { customerService } from './customerService';
  import { offerService } from './offerService';
  import type { Customer, OfferLLMRequest, OfferLLMResponse } from './types';

  interface Props {
    customerId: string;
  }

  let { customerId }: Props = $props();
  
  let customer: Customer | null = $state(null);
  let offer: OfferLLMResponse | null = $state(null);
  let isLoading = $state(true);
  let error: string | null = $state(null);
  let showAlternatives = $state(false);
  let showFullMarketSummary = $state(false);
  let solarPanelCount = $state(6);
  let isConfiguratorExpanded = $state(false);

  const scaledOffer = $derived.by(() => {
    if (!offer) return null;
    const factor = solarPanelCount / 6;

    return {
      ...offer,
      subsidies: offer.subsidies.map(s => ({
        ...s,
        estimated_effect_eur: Math.round(s.estimated_effect_eur * factor)
      })),
      recommended_offer: {
        ...offer.recommended_offer,
        estimated_cost_range_eur: {
          min: Math.round(offer.recommended_offer.estimated_cost_range_eur.min * factor),
          max: Math.round(offer.recommended_offer.estimated_cost_range_eur.max * factor)
        },
        estimated_annual_savings_eur: {
          min: Math.round(offer.recommended_offer.estimated_annual_savings_eur.min * factor),
          max: Math.round(offer.recommended_offer.estimated_annual_savings_eur.max * factor)
        }
      },
      alternative_offers: offer.alternative_offers.map(alt => ({
        ...alt,
        estimated_cost_range_eur: {
          min: Math.round(alt.estimated_cost_range_eur.min * factor),
          max: Math.round(alt.estimated_cost_range_eur.max * factor)
        }
      })),
      financing_options: offer.financing_options.map(f => ({
        ...f,
        down_payment_eur: Math.round(f.down_payment_eur * factor),
        loan_amount_eur: Math.round(f.loan_amount_eur * factor),
        monthly_payment_eur: Math.round(f.monthly_payment_eur * factor)
      }))
    };
  });

  function truncate(text: string, max = 140) {
    if (!text) return '';
    return text.length > max ? text.slice(0, max).trimEnd() + '…' : text;
  }

  onMount(async () => {
    try {
      customer = await customerService.getCustomerById(customerId);
      if (!customer) {
        error = 'Customer not found';
      } else {
        // Prepare request data from customer
        const metadata: Record<string, string> = {};
        if (customer.details) {
          Object.entries(customer.details).forEach(([key, value]) => {
            if (value) metadata[key] = value;
          });
        }

        const offerRequest: OfferLLMRequest = {
          property_info: {
            post_code: customer.address?.zip || '',
            street: customer.address?.street || '',
            house_number: customer.address?.houseNumber || '',
            city: customer.address?.city || '',
            country: 'Germany'
          },
          customer_profile: {
            birth_date: customer.birthDate,
            metadata: metadata
          }
        };

        offer = await offerService.generateOffer(offerRequest);
      }
    } catch (err) {
      console.error('Failed to load data:', err);
      error = 'Failed to load data';
    } finally {
      isLoading = false;
    }
  });

  function formatCurrency(value: number) {
    return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(value);
  }

  function goBack() {
    window.history.back();
  }
</script>

<div class="offer-page">
  {#if isLoading}
    <div class="loading-full">
      <div class="ai-loader-large">
        <div class="orbit"></div>
        <div class="core"></div>
      </div>
      <p>Generating your personal solution proposal...</p>
    </div>
  {:else if error}
    <div class="error-state card">
      <div class="error-icon">⚠️</div>
      <h2>Something went wrong</h2>
      <p>{error}</p>
      <button class="btn-primary" onclick={goBack}>Go Back</button>
    </div>
  {:else if scaledOffer && customer}
    <header class="offer-header">
      <div class="container header-container">
        <div class="header-content-left">
          <button class="btn-back-pill" onclick={goBack}>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
            <span>Back to Analysis</span>
          </button>
          <div class="header-title-group">
            <span class="badge badge-accent">Personalized Proposal</span>
            <h1>Sustainable Solution Proposal</h1>
            <p class="subtitle">Tailored for <strong>{customer.firstName} {customer.lastName}</strong> • {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</p>
          </div>
        </div>
        <div class="header-actions-top">
          <button class="btn-outline-dark" onclick={() => window.print()}>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 6 2 18 2 18 9"></polyline><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path><rect x="6" y="14" width="12" height="8"></rect></svg>
            <span>Print PDF</span>
          </button>
          <button class="btn-accept-proposal" onclick={() => window.location.href = 'https://codingbuddies.de'}>
            <div class="btn-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
            </div>
            <div class="btn-content">
              <span class="btn-title">Accept Proposal</span>
              <span class="btn-desc">Proceed to signature</span>
            </div>
          </button>
        </div>
      </div>
    </header>

    <main class="container">
      <div class="offer-grid">
        <!-- Interactive Solution Configurator -->
        {#if customer.details?.wantsSolarPanels}
          <section class="span-all configurator-section">
            <div class="card configurator-card">
              <button 
                class="card-header-toggle" 
                onclick={() => isConfiguratorExpanded = !isConfiguratorExpanded}
                aria-expanded={isConfiguratorExpanded}
              >
                <div class="panel-title">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="title-icon"><path d="M12 20v-6M6 20V10M18 20V4"/></svg>
                  Interactive Solution Configurator
                </div>
                <div class="toggle-icon {isConfiguratorExpanded ? 'expanded' : ''}">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
                </div>
              </button>
              {#if isConfiguratorExpanded}
                <div class="card-body" transition:slide={{ duration: 300 }}>
                  <div class="configurator-controls">
                    <div class="control-group">
                      <div class="control-label-row">
                        <label for="solar-panels">Solar Panels</label>
                        <span class="control-value">{solarPanelCount} Panels</span>
                      </div>
                      <input 
                        id="solar-panels" 
                        type="range" 
                        min="1" 
                        max="24" 
                        bind:value={solarPanelCount} 
                        class="config-slider"
                      />
                      <div class="range-labels">
                        <span>1 Panel</span>
                        <span>24 Panels</span>
                      </div>
                    </div>
                    <div class="configurator-info">
                      <p>Passen Sie die Anzahl der Solarpanels an, um die Auswirkungen auf Investitionskosten und Ersparnisse in Echtzeit zu sehen.</p>
                    </div>
                  </div>
                </div>
              {/if}
            </div>
          </section>
        {/if}

        <!-- Recommended Package - Hero Section -->
        <section class="span-all">
          <div class="hero-offer-card">
            <div class="hero-offer-content">
              <div class="hero-text-side">
                <div class="package-meta">
                  <span class="package-label">Recommended System</span>
                  <div class="efficiency-badge">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                    <span>High Efficiency</span>
                  </div>
                </div>
                <h2>{scaledOffer.recommended_offer.package_name}</h2>
                <p class="package-reason">{scaledOffer.recommended_offer.reasoning[0]}</p>
                
                <div class="included-grid">
                  {#each scaledOffer.recommended_offer.products as product}
                    <div class="included-item">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                      <span>{product}</span>
                    </div>
                  {/each}
                </div>
              </div>

              <div class="hero-stats-side">
                <div class="stat-box-large highlight">
                  <span class="stat-label">Total Investment</span>
                  <div class="stat-value-group">
                    <span class="stat-unit">€</span>
                    <span class="stat-number">{scaledOffer.recommended_offer.estimated_cost_range_eur.min.toLocaleString()}</span>
                  </div>
                  <span class="stat-sub">Inclusive of all local subsidies</span>
                </div>
                <div class="stat-box-large">
                  <span class="stat-label">Annual Savings</span>
                  <div class="stat-value-group success">
                    <span class="stat-unit">€</span>
                    <span class="stat-number">{scaledOffer.recommended_offer.estimated_annual_savings_eur.min.toLocaleString()}</span>
                  </div>
                  <span class="stat-sub">Est. {scaledOffer.recommended_offer.estimated_payback_years.min}y payback period</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Market Context & Drivers -->
        <section class="span-2">
          <div class="card h-full">
            <div class="card-header-simple">
              <div class="panel-title">Market Context & Outlook</div>
            </div>
            <div class="card-body-compact">
              <div class="compact-market-grid">
                <div class="market-summary-section">
                  <div class="context-intro-short">
                    {#if showFullMarketSummary}
                      <span>{scaledOffer.market_context.summary}</span>
                    {:else}
                      <span>{truncate(scaledOffer.market_context.summary, 160)}</span>
                    {/if}
                    {#if scaledOffer.market_context.summary && scaledOffer.market_context.summary.length > 160}
                      <button class="summary-toggle" onclick={() => showFullMarketSummary = !showFullMarketSummary}>
                        {showFullMarketSummary ? 'Weniger anzeigen' : 'Mehr anzeigen'}
                      </button>
                    {/if}
                  </div>
                </div>
                <div class="market-tags-section">
                  <div class="tag-row">
                    <span class="row-label">Drivers:</span>
                    <div class="pill-group">
                      {#each scaledOffer.market_context.drivers as driver}
                        <span class="pill-market">{driver}</span>
                      {/each}
                    </div>
                  </div>
                  <div class="tag-row">
                    <span class="row-label">Outlook:</span>
                    <div class="pill-group">
                      {#each scaledOffer.market_context.why_now as point}
                        <span class="pill-market accent">{point}</span>
                      {/each}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Subsidies Section -->
        <section class="span-1">
          <div class="card h-full subsidy-gradient">
            <div class="card-header-simple">
              <div class="panel-title">Available Subsidies</div>
            </div>
            <div class="card-body-padded">
              <div class="subsidies-stack">
                {#each scaledOffer.subsidies as subsidy}
                  <div class="subsidy-pill">
                    <div class="subsidy-info">
                      <span class="subsidy-name">{subsidy.name}</span>
                      <span class="subsidy-status">{subsidy.status}</span>
                    </div>
                    <div class="subsidy-value">-{formatCurrency(subsidy.estimated_effect_eur)}</div>
                  </div>
                {/each}
                <div class="subsidy-total">
                  <span>Total Estimated Benefits</span>
                  <strong>{formatCurrency(scaledOffer.subsidies.reduce((acc, s) => acc + s.estimated_effect_eur, 0))}</strong>
                </div>
              </div>
            </div>
          </div>
        </section>

        {#if scaledOffer.alternative_offers && scaledOffer.alternative_offers.length > 0}
          <section class="span-all alternatives-section">
            <div class="alternatives-header">
              <div class="header-text">
                <div class="panel-tag">Optional Extensions</div>
                <h3>Explore Complementary Solutions</h3>
                <p>Consider these alternative configurations to further enhance your energy independence.</p>
              </div>
              <button class="btn-toggle-alternatives {showAlternatives ? 'active' : ''}" onclick={() => showAlternatives = !showAlternatives}>
                <div class="toggle-icon">
                  {#if showAlternatives}
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"></polyline></svg>
                  {:else}
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
                  {/if}
                </div>
                <span>{showAlternatives ? 'Hide Alternatives' : 'Show Alternatives'}</span>
              </button>
            </div>

            {#if showAlternatives}
              <div class="alternatives-grid" transition:fade={{ duration: 300 }}>
                {#each scaledOffer.alternative_offers as alt}
                  <div class="alt-card">
                    <div class="alt-card-header">
                      <div class="alt-icon-box">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                      </div>
                      <div class="alt-title-group">
                        <span class="alt-label">Alternative Option</span>
                        <h4>{alt.package_name}</h4>
                      </div>
                    </div>
                    <div class="alt-body">
                      <p class="alt-positioning">{alt.positioning}</p>
                      <div class="alt-products-chips">
                        {#each alt.products as product}
                          <span class="alt-chip">{product}</span>
                        {/each}
                      </div>
                    </div>
                    <div class="alt-footer">
                      <div class="alt-price">
                        <span class="price-label">Est. Investment</span>
                        <span class="price-value">{formatCurrency(alt.estimated_cost_range_eur.min)}</span>
                      </div>
                      <button class="btn-select-alt">
                        <span>Select this option</span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                      </button>
                    </div>
                  </div>
                {/each}
              </div>
            {/if}
          </section>
        {/if}

        <!-- Financing Scenarios -->
        <section class="span-all">
          <div class="card">
            <div class="card-header-simple">
              <div class="panel-title">Flexible Financing Scenarios</div>
            </div>
            <div class="card-body-padded">
              <div class="financing-grid">
                {#each scaledOffer.financing_options as option}
                  <div class="financing-card-modern">
                    <div class="fin-header">
                      <h3>{option.scenario}</h3>
                      {#if option.monthly_payment_eur > 0}
                        <div class="fin-price">
                          <span class="price-val">{formatCurrency(option.monthly_payment_eur)}</span>
                          <span class="price-label">/ month</span>
                        </div>
                      {/if}
                    </div>
                    <div class="fin-body">
                      <div class="fin-stat-line">
                        <span>Down Payment</span>
                        <strong>{formatCurrency(option.down_payment_eur)}</strong>
                      </div>
                      {#if option.loan_amount_eur > 0}
                        <div class="fin-stat-line">
                          <span>Loan Amount</span>
                          <strong>{formatCurrency(option.loan_amount_eur)}</strong>
                        </div>
                        <div class="fin-stat-line">
                          <span>Duration / APR</span>
                          <strong>{option.term_months}mo / {option.apr_percent}%</strong>
                        </div>
                      {/if}
                    </div>
                    <div class="fin-footer">
                      <p>{option.notes}</p>
                    </div>
                  </div>
                {/each}
              </div>
            </div>
          </div>
        </section>
      </div>

      <footer class="offer-final-footer">
        <div class="disclaimer-box">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
          <p>{scaledOffer.disclaimer}</p>
        </div>
        <div class="action-footer">
          <p>Ready to power your home with {scaledOffer.recommended_offer.package_name}?</p>
          <div class="btn-group-center">
            <button class="btn-outline-dark btn-lg" onclick={goBack}>Save for Later</button>
            <button class="btn-primary btn-lg" onclick={() => window.location.href = 'mailto:support@codingbuddies.de'}>Contact Advisor</button>
          </div>
        </div>
      </footer>
    </main>
  {/if}
</div>

<style>
  .offer-page {
    background-color: var(--clr-bg-alt);
    min-height: 100vh;
  }

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem;
  }

  .header-container {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
  }

  .offer-header {
    background-color: white;
    padding: 4rem 0 3rem;
    border-bottom: 1px solid var(--clr-border);
    margin-bottom: 3rem;
  }

  .btn-back-pill {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    padding: 0.5rem 1rem;
    border-radius: var(--radius-full);
    color: var(--clr-text-muted);
    font-size: 0.8125rem;
    font-weight: 700;
    margin-bottom: 2rem;
    transition: all 0.2s;
  }

  .btn-back-pill:hover {
    background: white;
    color: var(--clr-primary);
    border-color: var(--clr-border-strong);
    transform: translateX(-4px);
  }

  .header-title-group h1 {
    font-size: 3rem;
    letter-spacing: -0.04em;
    margin: 0.5rem 0;
  }

  .subtitle {
    font-size: 1.125rem;
    color: var(--clr-text-muted);
  }

  .header-actions-top {
    display: flex;
    gap: 1rem;
    align-items: center;
  }

  .btn-accept-proposal {
    display: flex;
    align-items: center;
    gap: 1.25rem;
    background: linear-gradient(135deg, var(--clr-accent) 0%, #059669 100%);
    color: white;
    padding: 0.75rem 1.5rem;
    border-radius: var(--radius-md);
    border: none;
    box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.2), 0 4px 6px -4px rgba(16, 185, 129, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    text-align: left;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }

  .btn-accept-proposal::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transform: translateX(-100%);
    transition: transform 0.6s;
  }

  .btn-accept-proposal:hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 25px -5px rgba(16, 185, 129, 0.3), 0 8px 10px -6px rgba(16, 185, 129, 0.3);
  }

  .btn-accept-proposal:hover::before {
    transform: translateX(100%);
  }

  .btn-icon-wrapper {
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: transform 0.3s;
  }

  .btn-accept-proposal:hover .btn-icon-wrapper {
    transform: scale(1.1) rotate(5deg);
    background: white;
    color: var(--clr-accent);
  }

  .btn-content {
    display: flex;
    flex-direction: column;
  }

  .btn-title {
    font-size: 0.9375rem;
    font-weight: 700;
    line-height: 1.2;
    letter-spacing: -0.01em;
  }

  .btn-desc {
    font-size: 0.75rem;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.8);
  }

  .offer-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
  }

  .span-all { grid-column: 1 / -1; }
  .span-2 { grid-column: span 2; }
  .span-1 { grid-column: span 1; }

  .hero-offer-card {
    background: var(--clr-primary);
    color: white;
    border-radius: var(--radius-lg);
    padding: 3.5rem;
    position: relative;
    overflow: hidden;
    box-shadow: var(--shadow-xl);
  }

  .hero-offer-card::after {
    content: "";
    position: absolute;
    top: -20%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, rgba(255,255,255,0) 70%);
    pointer-events: none;
  }

  .hero-offer-content {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 4rem;
    position: relative;
    z-index: 1;
  }

  .package-meta {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .package-label {
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--clr-accent);
  }

  .efficiency-badge {
    background: rgba(16, 185, 129, 0.15);
    color: var(--clr-accent);
    padding: 0.25rem 0.75rem;
    border-radius: var(--radius-full);
    font-size: 0.75rem;
    font-weight: 800;
    display: flex;
    align-items: center;
    gap: 0.375rem;
    border: 1px solid rgba(16, 185, 129, 0.3);
  }

  .hero-text-side h2 {
    color: white;
    font-size: 2.75rem;
    margin-bottom: 1.5rem;
  }

  .package-reason {
    font-size: 1.25rem;
    line-height: 1.6;
    color: #cbd5e1;
    margin-bottom: 2.5rem;
    max-width: 600px;
  }

  .included-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .included-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-weight: 600;
    font-size: 0.9375rem;
    color: white;
  }

  .included-item svg {
    color: var(--clr-accent);
  }

  .hero-stats-side {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .stat-box-large {
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.1);
    padding: 2rem;
    border-radius: var(--radius-lg);
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .stat-box-large.highlight {
    background: rgba(255,255,255,0.1);
    border-color: rgba(255,255,255,0.2);
  }

  .stat-label {
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    color: #94a3b8;
  }

  .stat-value-group {
    display: flex;
    align-items: baseline;
    gap: 0.375rem;
  }

  .stat-value-group.success {
    color: var(--clr-accent);
  }

  .stat-unit {
    font-size: 1.25rem;
    font-weight: 700;
  }

  .stat-number {
    font-size: 2.5rem;
    font-weight: 800;
  }

  .stat-sub {
    font-size: 0.8125rem;
    color: #94a3b8;
    font-weight: 500;
  }

  .card-header-simple {
    padding: 1.5rem 2rem;
    border-bottom: 1px solid var(--clr-border);
  }

  .card-body-padded {
    padding: 2.5rem;
  }

  .card-body-compact {
    padding: 1.5rem 2rem;
  }

  .compact-market-grid {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .context-intro-short {
    font-size: 0.9375rem;
    line-height: 1.5;
    color: var(--clr-text-muted);
    margin: 0;
    max-width: 800px;
  }

  .market-tags-section {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .tag-row {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .row-label {
    font-size: 0.65rem;
    font-weight: 800;
    text-transform: uppercase;
    color: var(--clr-text-muted);
    min-width: 65px;
    letter-spacing: 0.05em;
  }

  .pill-group {
    display: flex;
    flex-wrap: wrap;
    gap: 0.4rem;
  }

  .pill-market {
    background: white;
    color: var(--clr-text-main);
    padding: 0.25rem 0.6rem;
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: 600;
    border: 1px solid var(--clr-border-strong);
    box-shadow: 0 1px 2px rgba(0,0,0,0.03);
    transition: all 0.2s ease;
  }

  .pill-market:hover {
    border-color: var(--clr-accent);
    transform: translateY(-1px);
  }

  .pill-market.accent {
    background: var(--clr-accent-light);
    color: var(--clr-accent-dark);
    border-color: var(--clr-accent);
  }

  .context-intro {
    font-size: 1.125rem;
    line-height: 1.6;
    margin-bottom: 2.5rem;
    color: var(--clr-text-muted);
  }

  .drivers-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 3rem;
  }

  .driver-column h4 {
    font-size: 0.75rem;
    font-weight: 800;
    text-transform: uppercase;
    color: var(--clr-text-muted);
    margin-bottom: 1.5rem;
  }

  .fancy-list {
    list-style: none;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .fancy-list li {
    position: relative;
    padding-left: 2rem;
    font-size: 0.9375rem;
    font-weight: 500;
    line-height: 1.5;
  }

  .fancy-list li::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0.25rem;
    width: 8px;
    height: 8px;
    background: var(--clr-border-strong);
    border-radius: 50%;
  }

  .fancy-list.accent li::before {
    background: var(--clr-accent);
    box-shadow: 0 0 10px var(--clr-accent);
  }

  .subsidy-gradient {
    background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
    border-color: var(--clr-accent-light);
  }

  .subsidies-stack {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .subsidy-pill {
    background: white;
    border: 1.5px solid var(--clr-border);
    padding: 1.25rem;
    border-radius: var(--radius-md);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .subsidy-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .subsidy-name {
    font-weight: 700;
    font-size: 0.9375rem;
  }

  .subsidy-status {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--clr-accent-dark);
    text-transform: uppercase;
  }

  .subsidy-value {
    font-size: 1.125rem;
    font-weight: 800;
    color: var(--clr-accent-dark);
  }

  /* Configurator Styles */
  .configurator-section {
    margin-bottom: 2rem;
  }

  .configurator-card {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border: 1px solid var(--clr-accent);
    overflow: hidden;
  }

  .card-header-toggle {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.25rem 1.5rem;
    background: transparent;
    border: none;
    cursor: pointer;
    text-align: left;
    transition: background-color 0.2s ease;
  }

  .card-header-toggle:hover {
    background-color: rgba(0, 0, 0, 0.02);
  }

  .toggle-icon {
    color: var(--clr-text-muted);
    transition: transform 0.3s ease;
  }

  .toggle-icon.expanded {
    transform: rotate(180deg);
  }

  .title-icon {
    margin-right: 0.5rem;
    color: var(--clr-accent-dark);
    vertical-align: middle;
  }

  .configurator-controls {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: center;
  }

  @media (max-width: 768px) {
    .configurator-controls {
      grid-template-columns: 1fr;
    }
  }

  .control-group {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .control-label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .control-label-row label {
    font-weight: 700;
    color: var(--clr-primary);
  }

  .control-value {
    font-weight: 800;
    color: var(--clr-accent-dark);
    background: white;
    padding: 0.25rem 0.75rem;
    border-radius: var(--radius-full);
    border: 1px solid var(--clr-accent);
    font-size: 0.875rem;
  }

  .config-slider {
    -webkit-appearance: none;
    width: 100%;
    height: 8px;
    border-radius: 4px;
    background: #e2e8f0;
    outline: none;
  }

  .config-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--clr-accent-dark);
    cursor: pointer;
    border: 2px solid white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }

  .config-slider::-moz-range-thumb {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--clr-accent-dark);
    cursor: pointer;
    border: 2px solid white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }

  .range-labels {
    display: flex;
    justify-content: space-between;
    font-size: 0.75rem;
    color: var(--clr-text-muted);
    font-weight: 600;
  }

  .configurator-info p {
    color: var(--clr-text-muted);
    font-size: 0.95rem;
    line-height: 1.5;
    margin: 0;
  }

  .subsidy-total {
    margin-top: 1rem;
    padding: 1.5rem;
    background: var(--clr-primary);
    color: white;
    border-radius: var(--radius-md);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .subsidy-total span {
    font-size: 0.875rem;
    font-weight: 600;
    color: #94a3b8;
  }

  .subsidy-total strong {
    font-size: 1.25rem;
    font-weight: 800;
    color: var(--clr-accent);
  }

  /* Alternatives Section Styles */
  .alternatives-section {
    margin: 4rem 0;
  }

  .alternatives-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 2.5rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid var(--clr-border);
  }

  .panel-tag {
    font-size: 0.75rem;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--clr-accent-dark);
    margin-bottom: 0.5rem;
  }

  .header-text h3 {
    font-size: 2rem;
    margin-bottom: 0.5rem;
    color: var(--clr-primary);
  }

  .header-text p {
    color: var(--clr-text-muted);
    font-size: 1.125rem;
  }

  .btn-toggle-alternatives {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1.5rem;
    background: white;
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-full);
    font-weight: 700;
    color: var(--clr-primary);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: var(--shadow-sm);
  }

  .btn-toggle-alternatives:hover {
    background: var(--clr-bg-alt);
    border-color: var(--clr-primary);
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
  }

  .btn-toggle-alternatives.active {
    background: var(--clr-primary);
    color: white;
    border-color: var(--clr-primary);
  }

  .toggle-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    background: rgba(0,0,0,0.05);
    border-radius: 50%;
    transition: transform 0.3s;
  }

  .btn-toggle-alternatives.active .toggle-icon {
    background: rgba(255,255,255,0.2);
  }

  .alternatives-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 2rem;
  }

  .alt-card {
    background: white;
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-lg);
    padding: 2rem;
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
  }

  .alt-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
    border-color: var(--clr-accent-dark);
  }

  .alt-card-header {
    display: flex;
    gap: 1.25rem;
    margin-bottom: 1.5rem;
  }

  .alt-icon-box {
    width: 48px;
    height: 48px;
    background: var(--clr-bg-alt);
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--clr-primary);
    flex-shrink: 0;
  }

  .alt-title-group {
    display: flex;
    flex-direction: column;
  }

  .alt-label {
    font-size: 0.625rem;
    font-weight: 800;
    text-transform: uppercase;
    color: var(--clr-accent-dark);
    margin-bottom: 0.25rem;
  }

  .alt-title-group h4 {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--clr-primary);
    margin: 0;
  }

  .alt-body {
    flex-grow: 1;
    margin-bottom: 2rem;
  }

  .alt-positioning {
    font-size: 0.9375rem;
    line-height: 1.6;
    color: var(--clr-text-muted);
    margin-bottom: 1.5rem;
  }

  .alt-products-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .alt-chip {
    padding: 0.375rem 0.75rem;
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-full);
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--clr-primary);
  }

  .alt-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 1.5rem;
    border-top: 1px solid var(--clr-border);
  }

  .alt-price {
    display: flex;
    flex-direction: column;
  }

  .alt-price .price-label {
    font-size: 0.625rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--clr-text-muted);
  }

  .alt-price .price-value {
    font-size: 1.125rem;
    font-weight: 800;
    color: var(--clr-primary);
  }

  .btn-select-alt {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 1rem;
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-md);
    font-size: 0.8125rem;
    font-weight: 700;
    color: var(--clr-primary);
    transition: all 0.2s;
  }

  .btn-select-alt:hover {
    background: var(--clr-primary);
    color: white;
    border-color: var(--clr-primary);
  }

  .financing-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .financing-card-modern {
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-lg);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .fin-header {
    padding: 1.5rem;
    background: white;
    border-bottom: 1px solid var(--clr-border);
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .fin-header h3 {
    font-size: 1rem;
    margin: 0;
  }

  .fin-price {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  .price-val {
    font-size: 1.25rem;
    font-weight: 800;
    color: var(--clr-accent-dark);
  }

  .price-label {
    font-size: 0.625rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--clr-text-muted);
  }

  .fin-body {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    flex-grow: 1;
  }

  .fin-stat-line {
    display: flex;
    justify-content: space-between;
    font-size: 0.875rem;
  }

  .fin-stat-line span {
    color: var(--clr-text-muted);
    font-weight: 500;
  }

  .fin-stat-line strong {
    color: var(--clr-primary);
    font-weight: 700;
  }

  .fin-footer {
    padding: 1rem 1.5rem;
    background: rgba(0,0,0,0.02);
    font-size: 0.75rem;
    color: var(--clr-text-muted);
    font-style: italic;
    line-height: 1.4;
  }

  .offer-final-footer {
    margin-top: 6rem;
    padding-bottom: 6rem;
    text-align: center;
  }

  .disclaimer-box {
    background: white;
    border: 1px solid var(--clr-border);
    padding: 1.5rem 2rem;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    gap: 1.5rem;
    max-width: 900px;
    margin: 0 auto 4rem;
    text-align: left;
  }

  .disclaimer-box p {
    font-size: 0.8125rem;
    color: var(--clr-text-muted);
    line-height: 1.6;
    margin: 0;
  }

  .disclaimer-box svg {
    flex-shrink: 0;
    color: var(--clr-border-strong);
  }

  .action-footer p {
    font-size: 1.5rem;
    font-weight: 800;
    margin-bottom: 2rem;
    color: var(--clr-primary);
  }

  .btn-group-center {
    display: flex;
    justify-content: center;
    gap: 1.5rem;
  }

  .btn-outline-dark {
    background: white;
    border: 1.5px solid var(--clr-primary);
    color: var(--clr-primary);
    padding: 0.75rem 1.5rem;
    border-radius: var(--radius-md);
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    gap: 0.75rem;
  }

  .btn-outline-dark:hover {
    background: var(--clr-bg-alt);
  }

  .btn-lg {
    padding: 1.25rem 2.5rem;
    font-size: 1.125rem;
    border-radius: var(--radius-lg);
  }

  .ai-loader-large {
    position: relative;
    width: 80px;
    height: 80px;
    margin-bottom: 2rem;
  }

  .error-state {
    text-align: center;
    padding: 5rem 2rem;
    max-width: 500px;
    margin: 5rem auto;
  }

  .error-icon {
    font-size: 3rem;
    margin-bottom: 1.5rem;
  }

  @media (max-width: 1024px) {
    .hero-offer-content {
      grid-template-columns: 1fr;
      gap: 3rem;
    }
    .financing-grid {
      grid-template-columns: 1fr 1fr;
    }
  }

  @media (max-width: 768px) {
    .header-container {
      flex-direction: column;
      align-items: flex-start;
      gap: 2rem;
    }
    .drivers-grid, .financing-grid {
      grid-template-columns: 1fr;
    }
    .offer-grid {
      grid-template-columns: 1fr;
    }
    .hero-offer-card {
      padding: 2rem;
    }
    .hero-text-side h2 {
      font-size: 2rem;
    }
    .btn-group-center {
      flex-direction: column;
    }
  }

  @media print {
    .offer-header, .btn-group-center, .action-footer, .btn-back-pill, .header-actions-top { display: none !important; }
    .offer-page { background: white; padding: 0; }
    .container { max-width: 100%; padding: 0; }
    .hero-offer-card { background: white; color: black; border: 2px solid black; box-shadow: none; }
    .hero-text-side h2, .package-reason, .included-item { color: black; }
    .stat-box-large { border: 1px solid #eee; color: black; }
    .stat-number { color: black !important; }
    .financing-card-modern { break-inside: avoid; }
    .card { box-shadow: none !important; border: 1px solid #ddd !important; }
  }
.summary-toggle{
  margin-left:8px;
  font-size:12px;
  color:var(--color-accent, #10b981);
  background:transparent;
  border:none;
  cursor:pointer;
  padding:0;
}
.summary-toggle:hover{ text-decoration: underline; }
</style>
