<script lang="ts">
  import { onMount } from 'svelte';
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
      <div class="spinner"></div>
      <p>Generating your personal offer...</p>
    </div>
  {:else if error}
    <div class="error-state card">
      <h2>Error</h2>
      <p>{error}</p>
      <button class="btn-primary" onclick={goBack}>Go Back</button>
    </div>
  {:else if offer && customer}
    <header class="offer-header">
      <div class="container">
        <button class="btn-back" onclick={goBack}>
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
          Back to Details
        </button>
        <h1>Personal Solution Proposal</h1>
        <p class="subtitle">Prepared for {customer.firstName} {customer.lastName}</p>
      </div>
    </header>

    <main class="container">
      <div class="grid">
        <!-- Recommended Package - The Main Attraction -->
        <section class="recommended-section span-all">
          <div class="recommended-card card highlight">
            <div class="card-badge">Most Recommended Solution</div>
            <div class="card-content">
              <div class="offer-main-info">
                <h2>{offer.recommended_offer.package_name}</h2>
                <div class="cost-summary">
                  <div class="cost-item">
                    <span class="label">Investment</span>
                    <span class="value">{formatCurrency(offer.recommended_offer.estimated_cost_range_eur.min)} - {formatCurrency(offer.recommended_offer.estimated_cost_range_eur.max)}</span>
                  </div>
                  <div class="cost-item">
                    <span class="label">Annual Savings</span>
                    <span class="value success">{formatCurrency(offer.recommended_offer.estimated_annual_savings_eur.min)} - {formatCurrency(offer.recommended_offer.estimated_annual_savings_eur.max)}</span>
                  </div>
                </div>
              </div>
              
              <div class="simple-details">
                <div class="reason-highlight">
                  <p>{offer.recommended_offer.reasoning[0]}</p>
                </div>
                <div class="key-products">
                  {#each offer.recommended_offer.products.slice(0, 3) as product}
                    <span class="product-tag">{product}</span>
                  {/each}
                  {#if offer.recommended_offer.products.length > 3}
                    <span class="product-tag">+{offer.recommended_offer.products.length - 3} more</span>
                  {/if}
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Market Context -->
        <section class="market-section span-all">
          <div class="card">
            <div class="panel-title">Market Context</div>
            <p class="market-summary">{offer.market_context.summary}</p>
            <div class="market-details-grid">
              <div class="market-list">
                <h4>Market Drivers</h4>
                <ul>
                  {#each offer.market_context.drivers as driver}
                    <li>{driver}</li>
                  {/each}
                </ul>
              </div>
              <div class="market-list">
                <h4>Why Hand Now?</h4>
                <ul>
                  {#each offer.market_context.why_now as point}
                    <li>{point}</li>
                  {/each}
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- Financing -->
        <section class="span-all">
          <div class="card bg-accent-light">
            <div class="panel-title">Financing Options</div>
            <div class="financing-grid">
              {#each offer.financing_options as option}
                <div class="financing-card">
                  <div class="financing-header">
                    <h3>{option.scenario}</h3>
                    {#if option.monthly_payment_eur > 0}
                      <span class="monthly-price">{formatCurrency(option.monthly_payment_eur)}/mo</span>
                    {/if}
                  </div>
                  <div class="financing-details">
                    <div class="fin-row">
                      <span>Down Payment</span>
                      <span>{formatCurrency(option.down_payment_eur)}</span>
                    </div>
                    {#if option.loan_amount_eur > 0}
                      <div class="fin-row">
                        <span>Loan Amount</span>
                        <span>{formatCurrency(option.loan_amount_eur)}</span>
                      </div>
                      <div class="fin-row">
                        <span>Term</span>
                        <span>{option.term_months} Months</span>
                      </div>
                      <div class="fin-row">
                        <span>APR</span>
                        <span>{option.apr_percent}%</span>
                      </div>
                    {/if}
                  </div>
                  <p class="fin-notes">{option.notes}</p>
                </div>
              {/each}
            </div>
          </div>
        </section>

        <!-- Subsidies -->
        <section class="span-all">
          <div class="card highlight-light">
            <div class="panel-title">Available Subsidies</div>
            <div class="subsidies-list">
              {#each offer.subsidies as subsidy}
                <div class="subsidy-item">
                  <div class="subsidy-header">
                    <h3>{subsidy.name}</h3>
                    <span class="subsidy-amount">-{formatCurrency(subsidy.estimated_effect_eur)}</span>
                  </div>
                  <p class="subsidy-relevance"><strong>Relevance:</strong> {subsidy.relevance}</p>
                  <p class="subsidy-notes">{subsidy.notes}</p>
                </div>
              {/each}
            </div>
          </div>
        </section>

      </div>

      <footer class="offer-footer">
        <p class="disclaimer">{offer.disclaimer}</p>
        <div class="footer-actions">
          <button class="btn-secondary" onclick={goBack}>Save as Draft</button>
          <button class="btn-primary" onclick={() => window.print()}>Print Proposal</button>
        </div>
      </footer>
    </main>
  {/if}
</div>

<style>
  .offer-page {
    background-color: #f8fafc;
    min-height: 100vh;
    padding-bottom: 4rem;
  }

  .container {
    max-width: 1100px;
    margin: 0 auto;
    padding: 0 1.5rem;
  }

  .offer-header {
    background-color: #ffffff;
    padding: 2.5rem 0;
    border-bottom: 1px solid #e2e8f0;
    margin-bottom: 2rem;
  }

  .btn-back {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: none;
    border: none;
    color: #64748b;
    font-size: 0.875rem;
    cursor: pointer;
    margin-bottom: 1rem;
    padding: 0;
    transition: color 0.2s;
  }

  .btn-back:hover {
    color: #0f172a;
  }

  h1 {
    font-size: 2.25rem;
    color: #0f172a;
    margin: 0;
    font-weight: 800;
  }

  .subtitle {
    color: #64748b;
    font-size: 1.125rem;
    margin: 0.5rem 0 0;
  }

  .grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
  }

  .span-all {
    grid-column: 1 / -1;
  }

  .card {
    background: white;
    padding: 2rem;
    border-radius: 1rem;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
    height: 100%;
  }

  .highlight {
    border: 2px solid #10b981;
    background: linear-gradient(135deg, #ffffff 0%, #f0fdf4 100%);
  }

  .highlight-light {
    border: 1px solid #10b981;
    background-color: #f0fdf4;
  }

  .bg-dark {
    background-color: #0f172a;
    color: #f8fafc;
  }

  .bg-accent-light {
    background-color: #f0f9ff;
  }

  .bg-warn-light {
    background-color: #fffbeb;
  }

  .text-warn {
    color: #d97706;
  }

  .card-badge {
    display: inline-block;
    background: #10b981;
    color: white;
    padding: 0.25rem 0.75rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    margin-bottom: 1rem;
  }

  .offer-main-info {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 2rem;
    margin-bottom: 2rem;
    padding-bottom: 2rem;
    border-bottom: 1px solid #e2e8f0;
  }

  .offer-main-info h2 {
    font-size: 1.875rem;
    color: #0f172a;
    margin: 0;
    flex: 1;
    min-width: 300px;
  }

  .cost-summary {
    display: flex;
    gap: 2.5rem;
  }

  .cost-item {
    display: flex;
    flex-direction: column;
  }

  .cost-item .label {
    font-size: 0.75rem;
    color: #64748b;
    text-transform: uppercase;
    font-weight: 600;
    letter-spacing: 0.025em;
  }

  .cost-item .value {
    font-size: 1.25rem;
    font-weight: 800;
    color: #0f172a;
  }

  .success {
    color: #059669 !important;
  }

  .simple-details {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .reason-highlight {
    font-size: 1.125rem;
    line-height: 1.6;
    color: #334155;
    border-left: 4px solid #10b981;
    padding-left: 1.5rem;
  }

  .key-products {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .product-tag {
    background: white;
    color: #475569;
    padding: 0.375rem 0.75rem;
    border-radius: 9999px;
    font-size: 0.8125rem;
    font-weight: 600;
    border: 1px solid #e2e8f0;
  }

  .market-summary {
    font-size: 1.125rem;
    line-height: 1.6;
    color: #334155;
    margin-bottom: 2rem;
  }

  .market-details-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2.5rem;
  }

  .market-list h4 {
    font-size: 0.75rem;
    text-transform: uppercase;
    color: #94a3b8;
    margin-bottom: 1rem;
    font-weight: 700;
  }

  .market-list ul {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .market-list li {
    font-size: 0.9375rem;
    color: #475569;
    padding-left: 1.5rem;
    position: relative;
  }

  .market-list li::before {
    content: "•";
    position: absolute;
    left: 0;
    color: #cbd5e1;
    font-weight: bold;
  }

  .financing-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .financing-card {
    background: white;
    padding: 1.5rem;
    border-radius: 0.75rem;
    border: 1px solid #e2e8f0;
    display: flex;
    flex-direction: column;
  }

  .financing-header {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-bottom: 1.25rem;
    padding-bottom: 1rem;
    border-bottom: 1px dashed #e2e8f0;
  }

  .financing-header h3 {
    font-size: 1rem;
    font-weight: 700;
    color: #0f172a;
    margin: 0;
  }

  .monthly-price {
    font-size: 1.125rem;
    font-weight: 800;
    color: #10b981;
  }

  .financing-details {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1rem;
  }

  .fin-row {
    display: flex;
    justify-content: space-between;
    font-size: 0.8125rem;
  }

  .fin-row span:first-child {
    color: #64748b;
  }

  .fin-row span:last-child {
    font-weight: 600;
    color: #0f172a;
  }

  .fin-notes {
    font-size: 0.75rem;
    color: #64748b;
    margin-top: auto;
    font-style: italic;
  }

  .subsidies-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .subsidy-item {
    background: white;
    padding: 1.5rem;
    border-radius: 0.75rem;
    border: 1px solid #d1fae5;
  }

  .subsidy-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
  }

  .subsidy-header h3 {
    font-size: 1.125rem;
    font-weight: 700;
    color: #0f172a;
    margin: 0;
  }

  .subsidy-amount {
    font-size: 1.25rem;
    font-weight: 800;
    color: #059669;
  }

  .subsidy-relevance {
    font-size: 0.875rem;
    color: #065f46;
    background: #ecfdf5;
    padding: 0.5rem 0.75rem;
    border-radius: 4px;
    margin-bottom: 0.75rem;
  }

  .subsidy-notes {
    font-size: 0.875rem;
    color: #475569;
    line-height: 1.5;
  }

  .missing-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
  }

  .missing-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    background: white;
    padding: 0.75rem 1rem;
    border-radius: 6px;
    border: 1px solid #fde68a;
    font-size: 0.8125rem;
    color: #92400e;
  }

  .benefits-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1.25rem;
    margin-top: 0.5rem;
  }

  .benefit-item {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    color: #cbd5e1;
    font-size: 0.9375rem;
    line-height: 1.5;
  }

  .alternatives-compact {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .alt-pill {
    background: white;
    border: 1px solid #e2e8f0;
    padding: 0.75rem 1.25rem;
    border-radius: 9999px;
    display: flex;
    gap: 1rem;
    align-items: center;
    transition: all 0.2s;
  }

  .alt-pill:hover {
    border-color: #cbd5e1;
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  }

  .alt-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: #334155;
  }

  .alt-price {
    font-size: 0.8125rem;
    font-weight: 700;
    color: #10b981;
  }

  .market-details {
    background: #f1f5f9;
    border-radius: 0.75rem;
    overflow: hidden;
  }

  .market-details summary {
    padding: 1rem 1.5rem;
    font-size: 0.875rem;
    font-weight: 600;
    color: #64748b;
    cursor: pointer;
    transition: background 0.2s;
  }

  .market-details summary:hover {
    background: #e2e8f0;
  }

  .market-content-inner {
    padding: 0 1.5rem 1.5rem;
    border-top: 1px solid #e2e8f0;
  }

  .market-content-inner p {
    font-size: 0.875rem;
    color: #475569;
    line-height: 1.6;
    margin: 1.5rem 0;
  }

  .context-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }

  .context-grid h4 {
    font-size: 0.75rem;
    text-transform: uppercase;
    color: #94a3b8;
    margin-bottom: 0.75rem;
  }

  .context-grid ul {
    list-style: none;
    padding: 0;
  }

  .context-grid li {
    font-size: 0.8125rem;
    color: #64748b;
    margin-bottom: 0.5rem;
    padding-left: 1rem;
    position: relative;
  }

  .context-grid li::before {
    content: "•";
    position: absolute;
    left: 0;
    color: #cbd5e1;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  li {
    position: relative;
    padding-left: 1.5rem;
    margin-bottom: 0.5rem;
    font-size: 0.9375rem;
    line-height: 1.5;
  }

  .panel-title {
    font-size: 0.875rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: #64748b;
    margin-bottom: 1.5rem;
  }

  .text-accent { color: #10b981; }

  .offer-footer {
    margin-top: 4rem;
    text-align: center;
    border-top: 1px solid #e2e8f0;
    padding-top: 2rem;
  }

  .disclaimer {
    font-size: 0.75rem;
    color: #94a3b8;
    max-width: 800px;
    margin: 0 auto 2rem;
    line-height: 1.5;
  }

  .footer-actions {
    display: flex;
    justify-content: center;
    gap: 1rem;
  }

  .btn-primary {
    background-color: #10b981;
    color: white;
    border: none;
    padding: 0.75rem 2rem;
    border-radius: 0.5rem;
    font-weight: 700;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-primary:hover {
    background-color: #059669;
  }

  .btn-secondary {
    background-color: white;
    color: #0f172a;
    border: 1px solid #e2e8f0;
    padding: 0.75rem 2rem;
    border-radius: 0.5rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-secondary:hover {
    border-color: #cbd5e1;
    background-color: #f8fafc;
  }

  .loading-full {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 60vh;
  }

  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #10b981;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  @media (max-width: 900px) {
    .grid { grid-template-columns: 1fr; }
    .context-grid, .benefits-grid { grid-template-columns: 1fr; gap: 1rem; }
    h1 { font-size: 1.75rem; }
  }

  @media print {
    .offer-header, .offer-footer, .btn-back, .market-section { display: none; }
    .offer-page { background: white; padding: 0; }
    .card { box-shadow: none; border: 1px solid #e2e8f0; }
    .highlight { border: 2px solid #10b981; }
    .bg-dark { background-color: white !important; color: black !important; }
    .text-accent { color: #10b981 !important; }
    .container { max-width: 100%; padding: 0; }
    .grid { gap: 0.5cm; }
  }
</style>
