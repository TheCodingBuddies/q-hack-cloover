<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import type { Customer, OfferData } from './types';

  interface Props {
    customerId: string;
  }

  let { customerId }: Props = $props();
  
  let customer: Customer | null = $state(null);
  let offer: OfferData | null = $state(null);
  let isLoading = $state(true);
  let error: string | null = $state(null);

  onMount(async () => {
    try {
      customer = await customerService.getCustomerById(customerId);
      if (!customer) {
        error = 'Customer not found';
      } else {
        // Mocking the offer data from todo.md
        offer = {
          "lead_summary": {
            "location": {
              "postal_code": "10115",
              "city": "Berlin",
              "country": "Germany"
            },
            "primary_product": "Solaranlage",
            "building_assumptions": [
              "Bestandsgebäude mit Baujahr 1995",
              "aktuell Gasheizungbasierte Heizung",
              "typischer Modernisierungsfall für ein Einfamilienhaus oder ähnliches Wohngebäude",
              "ohne Detaildaten zu Dämmstandard, Heizlast, Vorlauftemperatur und Heizkörperauslegung"
            ]
          },
          "market_context": {
            "summary": "Für einen Altbau mit Gasheizungheizung ist der Umstieg auf eine klimafreundliche Heizung wirtschaftlich und regulatorisch relevant, weil fossile Wärmekosten langfristig unter Druck bleiben, die 65%-EE-Vorgaben den Heizungsmarkt prägen und weiterhin attraktive Förderkulissen verfügbar sind.",
            "drivers": [
              "Gasheizungen bleiben anfällig für Preis- und CO2-Kostenrisiken.",
              "Das GEG setzt für neue Heizungen grundsätzlich den 65%-Erneuerbare-Energien-Rahmen.",
              "Die kommunale Wärmeplanung erhöht mittelfristig den Handlungsdruck und die Entscheidungssicherheit.",
              "Die Heizungsförderung kann die Anfangsinvestition deutlich reduzieren."
            ],
            "why_now": [
              "Fördermittel senken aktuell die Einstiegskosten spürbar.",
              "Ein Heizungstausch vor einem Defekt ist planbarer und oft wirtschaftlicher als eine Havarieentscheidung.",
              "Bei älteren Gasheizungen steigt typischerweise das Risiko für Reparaturen und Effizienzverluste.",
              "Frühes Handeln schafft mehr Auswahl bei Technik, Installationspartnern und Finanzierung."
            ]
          },
          "subsidies": [
            {
              "name": "KfW Heizungsförderung für Privatpersonen – Wohngebäude (458)",
              "status": "potenziell relevant",
              "relevance": "Sehr hoch bei selbstgenutztem Bestandswohngebäude und Tausch einer fossilen Heizung gegen eine klimafreundliche Heizung.",
              "estimated_effect_eur": 9000,
              "notes": "Die tatsächliche Förderhöhe hängt von Förderfähigkeit, förderfähigen Kosten und persönlichen Zuschlagsvoraussetzungen ab; in der Praxis ist eine Wärmepumpe häufig der zentrale Förderhebel."
            },
            {
              "name": "KfW Ergänzungskredit Wohngebäude (358/359)",
              "status": "potenziell relevant",
              "relevance": "Hoch, wenn Zuschuss bewilligt wurde und der Kunde Restkosten finanzieren möchte.",
              "estimated_effect_eur": 0,
              "notes": "Kein direkter Zuschuss, aber potenziell zinsgünstige Finanzierung; bei bestimmten Einkommensgrenzen ist ein zusätzlicher Zinsvorteil möglich."
            }
          ],
          "recommended_offer": {
            "package_name": "Empfohlen: Luft-Wasser-Wärmepumpe mit Heizungsoptimierung",
            "products": [
              "Luft-Wasser-Wärmepumpe im Bereich ca. 8-12 kW",
              "Hydraulischer Abgleich",
              "Pufferspeicher / Warmwasserspeicher je nach Auslegung",
              "Anpassung einzelner Heizflächen falls erforderlich",
              "Einbindung in bestehendes Wärmeverteilsystem",
              "Optional: Smart-Home- / Energiemanagement-Basis"
            ],
            "reasoning": [
              "Für einen typischen Wohngebäude-Bestand aus den 1980er-Jahren ist die Luft-Wasser-Wärmepumpe oft die wirtschaftlich sinnvollste Einstiegslösung, sofern die Systemtemperaturen beherrschbar sind.",
              "Der Wechsel von Gas auf Wärmepumpe adressiert das zentrale Kosten- und Regulierungsrisiko des Bestands.",
              "Ohne weitere Gebäudedaten ist eine konservative Empfehlung mit Fokus auf Heizungsoptimierung sinnvoller als ein technisch maximaler Ausbau.",
              "Die Förderlandschaft verbessert die Wirtschaftlichkeit deutlich und macht das Angebot vertrieblich gut argumentierbar."
            ],
            "estimated_cost_range_eur": { "min": 22000, "max": 34000 },
            "estimated_annual_savings_eur": { "min": 700, "max": 1800 },
            "estimated_payback_years": { "min": 8, "max": 15 }
          },
          "alternative_offers": [
            {
              "package_name": "Einsteiger: Wärmepumpe Basic",
              "products": ["Luft-Wasser-Wärmepumpe", "Standardinstallation", "Grundlegende Systemeinbindung"],
              "positioning": "Für Kunden mit Fokus auf schnellen Austausch, solide Förderung und geringere Anfangsinvestition.",
              "estimated_cost_range_eur": { "min": 18000, "max": 27000 }
            },
            {
              "package_name": "Komfort: Wärmepumpe + Heizflächen-/Effizienzupgrade",
              "products": ["Luft-Wasser-Wärmepumpe", "Hydraulischer Abgleich", "Teilweiser Heizkörpertausch", "Optimierung von Regelung und Speichertechnik"],
              "positioning": "Für Kunden, bei denen Vorlauftemperatur, Komfort oder Effizienz im Altbau gezielt verbessert werden sollen.",
              "estimated_cost_range_eur": { "min": 26000, "max": 38000 }
            },
            {
              "package_name": "Zukunftspaket: Wärmepumpe + PV-Vorbereitung",
              "products": ["Luft-Wasser-Wärmepumpe", "Energiemanagement-Vorbereitung", "Schnittstellen für spätere PV- oder Speicherintegration"],
              "positioning": "Für Kunden, die heute die Heizung modernisieren und in einem zweiten Schritt Solar nachziehen wollen.",
              "estimated_cost_range_eur": { "min": 23000, "max": 36000 }
            }
          ],
          "financing_options": [
            {
              "scenario": "Barzahlung",
              "down_payment_eur": 22000,
              "loan_amount_eur": 0,
              "term_months": 0,
              "apr_percent": 0.0,
              "monthly_payment_eur": 0,
              "notes": "Maximale Einfachheit; Förderung reduziert die effektive Nettobelastung im Nachgang bzw. gemäß Förderprozess."
            },
            {
              "scenario": "Teilfinanzierung",
              "down_payment_eur": 7000,
              "loan_amount_eur": 16000,
              "term_months": 120,
              "apr_percent": 4.9,
              "monthly_payment_eur": 169,
              "notes": "Geeignet für Kunden, die Eigenkapital einsetzen, aber Liquidität schonen möchten."
            },
            {
              "scenario": "Vollfinanzierung",
              "down_payment_eur": 0,
              "loan_amount_eur": 24000,
              "term_months": 180,
              "apr_percent": 5.2,
              "monthly_payment_eur": 192,
              "notes": "Für Kunden mit geringem Eigenkapital; Kombination mit Fördermitteln und ggf. Ergänzungskredit prüfen."
            }
          ],
          "sales_talking_points": [
            "Sie tauschen ein fossiles Preisrisiko gegen ein zukunftsfähiges Heizsystem mit Förderung.",
            "Gerade bei einem Gebäude aus den 1980ern ist eine Wärmepumpe oft wirtschaftlich machbar, wenn die Systemauslegung sauber geplant wird.",
            "Heute geplant ist günstiger und stressfreier als später unter Defektdruck.",
            "Wir empfehlen nicht pauschal das teuerste Paket, sondern die Variante mit dem besten Verhältnis aus Investition, Förderung und Betriebskosten."
          ],
          "missing_information": [
            "Gebäudetyp und Wohnfläche",
            "Dämmstandard / Sanierungsstand",
            "Jährlicher Gasverbrauch in kWh",
            "Aktuelles Heizsystemalter",
            "Vorlauftemperaturen im Winter",
            "Art und Größe der Heizkörper / Flächenheizung",
            "Anzahl der Bewohner",
            "Ob Selbstnutzung oder Vermietung vorliegt",
            "Verfügbares Eigenkapital und gewünschte Monatsrate"
          ],
          "disclaimer": "Dieses Angebot ist fiktiv und dient der Vertriebs-Vorqualifizierung. Förderfähigkeit, technische Machbarkeit, exakte Dimensionierung, reale Einsparungen und Finanzierungskonditionen müssen vor Vertragsabschluss durch Vor-Ort-Termin, Verbrauchsdaten, Heizlastprüfung und finale Förderprüfung validiert werden."
        };
      }
    } catch (err) {
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

        <!-- Financing and Subsidies -->
        <section class="compact-info">
          <div class="card bg-accent-light">
            <div class="panel-title">Smart Financing</div>
            <div class="financing-simple">
              {#each offer.financing_options.filter(o => o.monthly_payment_eur > 0).slice(0, 1) as option}
                <div class="main-scenario">
                  <span class="price-big">{formatCurrency(option.monthly_payment_eur)}</span>
                  <span class="price-label">per month</span>
                  <p class="scenario-note">{option.scenario} with {formatCurrency(option.down_payment_eur)} down payment</p>
                </div>
              {/each}
            </div>
          </div>
        </section>

        <section class="compact-info">
          <div class="card highlight-light">
            <div class="panel-title">Available Subsidies</div>
            <div class="subsidy-simple">
              {#each offer.subsidies.filter(s => s.estimated_effect_eur > 0) as subsidy}
                <div class="subsidy-pill">
                  <span class="subsidy-name">{subsidy.name}</span>
                  <span class="subsidy-value">-{formatCurrency(subsidy.estimated_effect_eur)}</span>
                </div>
              {/each}
            </div>
          </div>
        </section>

        <!-- Talking Points -->
        <section class="talking-points span-all">
          <div class="card bg-dark">
            <div class="panel-title text-accent">Key Benefits for You</div>
            <div class="benefits-grid">
              {#each offer.sales_talking_points as point}
                <div class="benefit-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{point}</span>
                </div>
              {/each}
            </div>
          </div>
        </section>

        <!-- Alternative Offers (more compact) -->
        <section class="alternatives-section span-all">
          <div class="panel-title">Alternative Options</div>
          <div class="alternatives-compact">
            {#each offer.alternative_offers as alt}
              <div class="alt-pill">
                <span class="alt-name">{alt.package_name}</span>
                <span class="alt-price">from {formatCurrency(alt.estimated_cost_range_eur.min)}</span>
              </div>
            {/each}
          </div>
        </section>

        <!-- Market Context (Hidden by default or simplified) -->
        <section class="market-section span-all">
          <details class="market-details">
            <summary>Market Insights & Building Assumptions</summary>
            <div class="market-content-inner">
              <p>{offer.market_context.summary}</p>
              <div class="context-grid">
                <div>
                  <h4>Why now?</h4>
                  <ul>
                    {#each offer.market_context.why_now.slice(0, 3) as point}
                      <li>{point}</li>
                    {/each}
                  </ul>
                </div>
                <div>
                  <h4>Building Status</h4>
                  <ul>
                    {#each offer.lead_summary.building_assumptions.slice(0, 3) as assumption}
                      <li>{assumption}</li>
                    {/each}
                  </ul>
                </div>
              </div>
            </div>
          </details>
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
    background-color: #ecfdf5;
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
    background: #f1f5f9;
    color: #475569;
    padding: 0.375rem 0.75rem;
    border-radius: 9999px;
    font-size: 0.8125rem;
    font-weight: 600;
    border: 1px solid #e2e8f0;
  }

  .compact-info .card {
    padding: 1.5rem;
  }

  .financing-simple {
    text-align: center;
    padding: 1rem 0;
  }

  .price-big {
    display: block;
    font-size: 2.5rem;
    font-weight: 900;
    color: #10b981;
    line-height: 1;
  }

  .price-label {
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 600;
    text-transform: uppercase;
  }

  .scenario-note {
    margin-top: 1rem;
    font-size: 0.8125rem;
    color: #475569;
  }

  .subsidy-simple {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .subsidy-pill {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: white;
    padding: 0.75rem 1rem;
    border-radius: 0.75rem;
    border: 1px solid #d1fae5;
  }

  .subsidy-name {
    font-size: 0.8125rem;
    font-weight: 600;
    color: #334155;
    max-width: 70%;
  }

  .subsidy-value {
    font-weight: 800;
    color: #059669;
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
