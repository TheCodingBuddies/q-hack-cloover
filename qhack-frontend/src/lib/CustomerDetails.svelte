<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import { offerService } from './offerService';
  import type { Customer, OfferLLMRequest, OfferLLMResponse } from './types';

  interface Project {
    id: string;
    name: string;
    location: string;
    offerPreview: string;
    aiHints: string[];
  }

  interface Props {
    customerId: string;
  }

  let { customerId }: Props = $props();
  
  let customer: Customer | null = $state(null);
  let isLoading = $state(true);
  let error: string | null = $state(null);

  // Single mock project for the customer
  let project: Project | null = $state(null);
  let offer: OfferLLMResponse | null = $state(null);

  onMount(async () => {
    try {
      customer = await customerService.getCustomerById(customerId);
      if (!customer) {
        error = 'Customer not found';
      } else {
        // Mock a single project for the UI demonstration, similar to AllCustomers.svelte
        const city = customer.address?.city || 'Unknown';
        const fullAddress = customer.address 
          ? `${customer.address.street} ${customer.address.houseNumber}, ${customer.address.zip} ${customer.address.city}`
          : 'No address provided';

        project = {
          id: 'p1',
          name: 'Solar Installation ' + city,
          location: fullAddress,
          offerPreview: `**Offer #2024-001**\n\nSolar panel installation for residential property.\n\n- 12x 400W panels\n- 1x 10kWh battery storage\n- Grid feed-in setup\n\n**Total: €18,400**\n\nValidity: 30 days`,
          aiHints: [
            'Ask about current electricity bill to calculate ROI',
            'Roof orientation is south-west — ideal for yield',
            'Customer mentioned interest in EV charging in future',
          ],
        };

        // Generate offer automatically after loading customer data
        try {
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
          
          console.log('Generating offer with request:', offerRequest);
          offerService.generateOffer(offerRequest)
            .then(res => {
              console.log('Offer generated successfully:', res);
              offer = res;
            })
            .catch(err => console.error('Failed to generate offer:', err));
        } catch (err) {
          console.error('Error preparing offer request:', err);
        }
      }
    } catch (err) {
      error = 'Failed to load customer data';
    } finally {
      isLoading = false;
    }
  });

  // Edit customer
  let editingCustomer = $state(false);
  let editCustomerForm = $state({ 
    firstName: '', 
    lastName: '', 
    birthDate: '', 
    street: '', 
    houseNumber: '', 
    zip: '', 
    city: '', 
    customerProfile: '', 
    energyConsumption: '', 
    existingSystems: '', 
    financialProfile: '', 
    conversationHistory: '',
    wantsHeatPump: false,
    wantsSolarPanels: false,
    wantsWallbox: false
  });

  function openEditCustomer() {
    if (!customer) return;
    editCustomerForm = {
      firstName: customer.firstName,
      lastName: customer.lastName,
      birthDate: customer.birthDate,
      street: customer.address?.street || '',
      houseNumber: customer.address?.houseNumber || '',
      zip: customer.address?.zip || '',
      city: customer.address?.city || '',
      customerProfile: customer.details?.customerProfile || '',
      energyConsumption: customer.details?.energyConsumption || '',
      existingSystems: customer.details?.existingSystems || '',
      financialProfile: customer.details?.financialProfile || '',
      conversationHistory: customer.details?.conversationHistory || '',
      wantsHeatPump: !!customer.details?.wantsHeatPump,
      wantsSolarPanels: !!customer.details?.wantsSolarPanels,
      wantsWallbox: !!customer.details?.wantsWallbox,
    };
    editingCustomer = true;
  }

  function saveEditCustomer() {
    if (!customer) return;
    customer.firstName = editCustomerForm.firstName;
    customer.lastName = editCustomerForm.lastName;
    customer.birthDate = editCustomerForm.birthDate;
    customer.address = {
      street: editCustomerForm.street,
      houseNumber: editCustomerForm.houseNumber,
      zip: editCustomerForm.zip,
      city: editCustomerForm.city,
    };
    customer.details = {
      customerProfile: editCustomerForm.customerProfile,
      energyConsumption: editCustomerForm.energyConsumption,
      existingSystems: editCustomerForm.existingSystems,
      financialProfile: editCustomerForm.financialProfile,
      conversationHistory: editCustomerForm.conversationHistory,
      wantsHeatPump: editCustomerForm.wantsHeatPump,
      wantsSolarPanels: editCustomerForm.wantsSolarPanels,
      wantsWallbox: editCustomerForm.wantsWallbox,
    };
    editingCustomer = false;
  }

  function onModalKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape') {
      editingCustomer = false;
    }
  }
</script>

<div class="layout">
  {#if isLoading}
    <div class="loading-full">
      <div class="spinner"></div>
      <p>Loading customer data...</p>
    </div>
  {:else if error && !customer}
    <div class="error-state card">
      <h2>Error</h2>
      <p>{error}</p>
      <button class="btn-primary" onclick={() => window.history.back()}>Go Back</button>
    </div>
  {:else if customer}
    <!-- Main content -->
    <main class="main">
      {#if project}
        <div class="main-header">
          <div class="main-header-left">
            <div class="breadcrumb">
              <span onclick={() => window.history.back()} role="link" tabindex="0" onkeydown={(e) => e.key === 'Enter' && window.history.back()}>Customers</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
              <span>{customer.firstName} {customer.lastName}</span>
            </div>
            <div class="main-header-title-row">
              <div class="title-with-tag">
                <span class="project-tag">Solar Project</span>
                <h2>{project.name}</h2>
              </div>
              <div class="header-actions">
                <button class="btn-solution-proposal" onclick={() => {
                  window.history.pushState({}, '', `/customer/${customerId}/offer`);
                  window.dispatchEvent(new PopStateEvent('popstate'));
                }}>
                  <div class="btn-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path></svg>
                  </div>
                  <div class="btn-text">
                    <span class="btn-label">Solution Proposal</span>
                    <span class="btn-sublabel">Open Detailed View</span>
                  </div>
                  <svg class="chevron-right" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="main-body">
          <div class="content-left">
            <!-- OFFER OVERVIEW -->
            <div class="offer-preview-card card">
              <div class="card-header-accent">
                <div class="panel-title">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                  <span>Analysis Overview</span>
                </div>
                {#if offer}
                  <span class="status-badge pulse">AI Generated</span>
                {/if}
              </div>
              
              {#if offer}
                <div class="offer-content">
                  <div class="summary-highlight">
                    <div class="highlight-item">
                      <span class="label">Location Analysis</span>
                      <span class="value">{offer.lead_summary.location.city} • {offer.lead_summary.location.postal_code}</span>
                    </div>
                    <div class="highlight-item">
                      <span class="label">Product Focus</span>
                      <div class="tag-group">
                        {#each offer.lead_summary.primary_products as product}
                          <span class="badge badge-accent">{product}</span>
                        {/each}
                      </div>
                    </div>
                  </div>

                  <div class="recommendation-box">
                    <div class="recommendation-header">
                      <div class="rec-title">
                        <span class="label">Selected Solution</span>
                        <h3>{offer.recommended_offer.package_name}</h3>
                      </div>
                      <div class="efficiency-score">
                        <span class="score-label">Efficiency</span>
                        <span class="score-value">High</span>
                      </div>
                    </div>
                    
                    <div class="details-grid">
                      <div class="detail-column">
                        <span class="small-label">Technical Configuration</span>
                        <ul class="check-list">
                          {#each offer.recommended_offer.products as product}
                            <li>
                              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                              <span>{product}</span>
                            </li>
                          {/each}
                        </ul>
                      </div>

                      <div class="detail-column">
                        <span class="small-label">Strategic Reasoning</span>
                        <ul class="dot-list">
                          {#each offer.recommended_offer.reasoning as reason}
                            <li>{reason}</li>
                          {/each}
                        </ul>
                      </div>
                    </div>

                    <div class="financial-dashboard">
                      <div class="fin-stat">
                        <span class="label">Investment</span>
                        <div class="value-row">
                          <span class="unit">€</span>
                          <span class="number">{offer.recommended_offer.estimated_cost_range_eur.min.toLocaleString()}</span>
                          <span class="range">- {offer.recommended_offer.estimated_cost_range_eur.max.toLocaleString()}</span>
                        </div>
                      </div>
                      <div class="fin-stat positive">
                        <span class="label">Est. Annual Savings</span>
                        <div class="value-row">
                          <span class="unit">€</span>
                          <span class="number">{offer.recommended_offer.estimated_annual_savings_eur.min.toLocaleString()}</span>
                        </div>
                      </div>
                      <div class="fin-stat">
                        <span class="label">ROI Period</span>
                        <div class="value-row">
                          <span class="number">{offer.recommended_offer.estimated_payback_years.min}</span>
                          <span class="unit">Years</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              {:else}
                <div class="loading-container">
                  <div class="ai-loader">
                    <div class="orbit"></div>
                    <div class="core"></div>
                  </div>
                  <h3>Thinking...</h3>
                  <p>AI is analyzing property data and local market context to generate the optimal proposal.</p>
                </div>
              {/if}
            </div>
          </div>

          <!-- Right panels -->
          <div class="right-panels">
            <div class="sidebar-card card">
              <div class="sidebar-header">
                <div class="panel-title">Customer Profile</div>
                <button class="btn-icon-only" onclick={openEditCustomer} aria-label="Edit Profile">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
              </div>
              
              <div class="profile-summary">
                <div class="profile-avatar">{customer.firstName[0]}{customer.lastName[0]}</div>
                <div class="profile-info">
                  <span class="profile-name">{customer.firstName} {customer.lastName}</span>
                  <span class="profile-id">ID: {customerId.substring(0, 8)}</span>
                </div>
              </div>

              <div class="data-list">
                <div class="data-item">
                  <div class="data-label">Address</div>
                  <div class="data-value">
                    {#if customer.address}
                      {customer.address.street} {customer.address.houseNumber}<br/>
                      {customer.address.zip} {customer.address.city}
                    {:else}
                      <span class="text-muted">Not provided</span>
                    {/if}
                  </div>
                </div>

                {#if customer.details}
                  <div class="data-divider"></div>

                  <div class="data-item">
                    <div class="data-label">Interests</div>
                    <div class="interests-badges">
                      {#if customer.details.wantsHeatPump}
                        <span class="interest-badge">Heat Pump</span>
                      {/if}
                      {#if customer.details.wantsSolarPanels}
                        <span class="interest-badge">Solar Panels</span>
                      {/if}
                      {#if customer.details.wantsWallbox}
                        <span class="interest-badge">Wallbox</span>
                      {/if}
                      {#if !customer.details.wantsHeatPump && !customer.details.wantsSolarPanels && !customer.details.wantsWallbox}
                        <span class="text-muted" style="font-size: 0.8125rem;">None selected</span>
                      {/if}
                    </div>
                  </div>

                  <div class="data-divider"></div>
                  
                  {#if customer.details.customerProfile}
                    <div class="data-item">
                      <div class="data-label">Household</div>
                      <div class="data-value-small">{customer.details.customerProfile}</div>
                    </div>
                  {/if}
                  
                  {#if customer.details.energyConsumption}
                    <div class="data-item">
                      <div class="data-label">Energy Needs</div>
                      <div class="data-value-small">{customer.details.energyConsumption}</div>
                    </div>
                  {/if}
                  
                  {#if customer.details.financialProfile}
                    <div class="data-item">
                      <div class="data-label">Financials</div>
                      <div class="data-value-small">{customer.details.financialProfile}</div>
                    </div>
                  {/if}
                {/if}
              </div>
            </div>

            <div class="sidebar-card card ai-accent">
              <div class="sidebar-header">
                <div class="panel-title ai-sparkle">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>
                  <span>Sales Coach Advice</span>
                </div>
              </div>
              <div class="ai-hints-body">
                <ul class="ai-hint-list">
                  {#if offer}
                    {#each offer.sales_talking_points as hint}
                      <li>{hint}</li>
                    {/each}
                  {:else}
                    {#each project.aiHints as hint}
                      <li>{hint}</li>
                    {/each}
                  {/if}
                </ul>
              </div>
            </div>
          </div>
        </div>
      {:else}
        <div class="empty-state">
          <div class="empty-illustration">📄</div>
          <h2>Initialize Analysis</h2>
          <p>Complete the customer profile to start generating AI sales hints and proposals.</p>
          <button class="btn-primary" onclick={openEditCustomer}>Edit Profile</button>
        </div>
      {/if}
    </main>
  {/if}
</div>

<!-- Modals -->
{#if editingCustomer && customer}
  <div class="modal-backdrop" role="presentation" onkeydown={onModalKeydown}>
    <div class="modal-card card" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="edit-customer-title">
      <div class="modal-header">
        <h2 id="edit-customer-title">Edit customer info</h2>
        <p class="modal-subtitle">Update the details for {customer.firstName} {customer.lastName}.</p>
      </div>
      <form onsubmit={(e) => { e.preventDefault(); saveEditCustomer(); }}>
        <div class="modal-grid">
          <div class="modal-form-group">
            <label for="ec-firstName">First name *</label>
            <input id="ec-firstName" type="text" bind:value={editCustomerForm.firstName} placeholder="First name" />
          </div>
          <div class="modal-form-group">
            <label for="ec-lastName">Last name *</label>
            <input id="ec-lastName" type="text" bind:value={editCustomerForm.lastName} placeholder="Last name" />
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-birthDate">Date of birth *</label>
            <input id="ec-birthDate" type="date" bind:value={editCustomerForm.birthDate} />
          </div>
          <div class="modal-form-group">
            <label for="ec-street">Street *</label>
            <input id="ec-street" type="text" bind:value={editCustomerForm.street} placeholder="Street" />
          </div>
          <div class="modal-form-group">
            <label for="ec-houseNumber">House No. *</label>
            <input id="ec-houseNumber" type="text" bind:value={editCustomerForm.houseNumber} placeholder="No." />
          </div>
          <div class="modal-form-group">
            <label for="ec-zip">ZIP *</label>
            <input id="ec-zip" type="text" bind:value={editCustomerForm.zip} placeholder="ZIP code" />
          </div>
          <div class="modal-form-group">
            <label for="ec-city">City *</label>
            <input id="ec-city" type="text" bind:value={editCustomerForm.city} placeholder="City" />
          </div>

          <hr class="modal-divider modal-full" />

          <div class="modal-form-group modal-full">
            <label>Product Interests</label>
            <div class="modal-checkbox-grid">
              <label class="modal-checkbox">
                <input type="checkbox" bind:checked={editCustomerForm.wantsHeatPump} />
                <span>Heat Pump</span>
              </label>
              <label class="modal-checkbox">
                <input type="checkbox" bind:checked={editCustomerForm.wantsSolarPanels} />
                <span>Solar Panels</span>
              </label>
              <label class="modal-checkbox">
                <input type="checkbox" bind:checked={editCustomerForm.wantsWallbox} />
                <span>Wallbox</span>
              </label>
            </div>
          </div>

          <hr class="modal-divider modal-full" />
          
          <div class="modal-form-group modal-full">
            <label for="ec-profile">Customer Profile</label>
            <textarea id="ec-profile" bind:value={editCustomerForm.customerProfile} placeholder="Details about the customer..."></textarea>
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-energy">Energy Consumption</label>
            <textarea id="ec-energy" bind:value={editCustomerForm.energyConsumption} placeholder="Details about energy needs..."></textarea>
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-systems">Existing Systems</label>
            <textarea id="ec-systems" bind:value={editCustomerForm.existingSystems} placeholder="Details about current setup..."></textarea>
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-finance">Financial Profile</label>
            <textarea id="ec-finance" bind:value={editCustomerForm.financialProfile} placeholder="Financial details..."></textarea>
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-history">Conversation History</label>
            <textarea id="ec-history" bind:value={editCustomerForm.conversationHistory} placeholder="Notes from previous talks..."></textarea>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" onclick={() => editingCustomer = false}>Cancel</button>
          <button type="submit" class="btn-modal-save">Save changes</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<style>
  .layout {
    display: block;
    width: 100%;
  }

  .main {
    width: 100%;
    padding: 0;
  }

  .main-header {
    margin-bottom: 3rem;
  }

  .breadcrumb {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--clr-text-muted);
    font-size: 0.875rem;
    font-weight: 500;
    margin-bottom: 1.5rem;
  }

  .breadcrumb span:first-child {
    cursor: pointer;
    transition: color 0.2s;
  }

  .breadcrumb span:first-child:hover {
    color: var(--clr-primary);
  }

  .title-with-tag {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .project-tag {
    align-self: flex-start;
    padding: 0.25rem 0.75rem;
    background: var(--clr-accent-light);
    color: var(--clr-accent-dark);
    border-radius: var(--radius-full);
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .main-header-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 2rem;
  }

  .main-header h2 {
    font-size: 2.5rem;
    font-weight: 800;
    margin: 0;
    letter-spacing: -0.04em;
  }

  .btn-solution-proposal {
    display: flex;
    align-items: center;
    gap: 1.25rem;
    background: linear-gradient(135deg, var(--clr-primary) 0%, #1e293b 100%);
    color: white;
    padding: 0.875rem 1.5rem;
    border-radius: var(--radius-md);
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 10px 15px -3px rgba(15, 23, 42, 0.2), 0 4px 6px -4px rgba(15, 23, 42, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    text-align: left;
    position: relative;
    overflow: hidden;
  }

  .btn-solution-proposal::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.05), transparent);
    transform: translateX(-100%);
    transition: transform 0.6s;
  }

  .btn-solution-proposal:hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.25), 0 8px 10px -6px rgba(15, 23, 42, 0.25);
    border-color: var(--clr-accent);
  }

  .btn-solution-proposal:hover::after {
    transform: translateX(100%);
  }

  .btn-icon {
    width: 40px;
    height: 40px;
    background: rgba(16, 185, 129, 0.15);
    color: var(--clr-accent);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.3s;
  }

  .btn-solution-proposal:hover .btn-icon {
    background: var(--clr-accent);
    color: white;
    transform: scale(1.1);
  }

  .btn-text {
    display: flex;
    flex-direction: column;
  }

  .btn-label {
    font-size: 1rem;
    font-weight: 700;
    line-height: 1.2;
  }

  .btn-sublabel {
    font-size: 0.75rem;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.6);
  }

  .chevron-right {
    color: rgba(255, 255, 255, 0.4);
    transition: all 0.3s;
    margin-left: 0.5rem;
  }

  .btn-solution-proposal:hover .chevron-right {
    color: white;
    transform: translateX(4px);
  }

  .main-body {
    display: grid;
    grid-template-columns: 1fr 360px;
    gap: 2.5rem;
    align-items: start;
  }

  .offer-preview-card {
    padding: 0;
    overflow: hidden;
  }

  .card-header-accent {
    padding: 1.5rem 2rem;
    background: var(--clr-bg-alt);
    border-bottom: 1px solid var(--clr-border);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.875rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--clr-primary);
  }

  .status-badge {
    font-size: 0.75rem;
    font-weight: 700;
    color: var(--clr-accent-dark);
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .status-badge::before {
    content: "";
    width: 8px;
    height: 8px;
    background: var(--clr-accent);
    border-radius: 50%;
  }

  .pulse::before {
    animation: pulse 2s infinite;
  }

  @keyframes pulse {
    0% { transform: scale(1); opacity: 1; }
    50% { transform: scale(1.5); opacity: 0.5; }
    100% { transform: scale(1); opacity: 1; }
  }

  .offer-content {
    padding: 2.5rem;
    display: flex;
    flex-direction: column;
    gap: 2.5rem;
  }

  .summary-highlight {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }

  .highlight-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .label {
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--clr-text-muted);
    letter-spacing: 0.05em;
  }

  .value {
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--clr-primary);
  }

  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .recommendation-box {
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-lg);
    padding: 2rem;
  }

  .recommendation-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 2rem;
  }

  .rec-title h3 {
    font-size: 1.5rem;
    margin: 0.25rem 0 0;
  }

  .efficiency-score {
    text-align: right;
  }

  .score-label {
    font-size: 0.625rem;
    font-weight: 700;
    text-transform: uppercase;
    display: block;
    color: var(--clr-text-muted);
  }

  .score-value {
    color: var(--clr-accent-dark);
    font-weight: 800;
    font-size: 1.25rem;
  }

  .details-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2.5rem;
    margin-bottom: 2.5rem;
  }

  .small-label {
    font-size: 0.6875rem;
    font-weight: 800;
    text-transform: uppercase;
    color: var(--clr-text-muted);
    margin-bottom: 1rem;
    display: block;
  }

  .check-list, .dot-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .check-list li {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.9375rem;
    font-weight: 600;
  }

  .check-list svg {
    color: var(--clr-accent);
    flex-shrink: 0;
  }

  .dot-list li {
    position: relative;
    padding-left: 1.25rem;
    font-size: 0.875rem;
    color: var(--clr-text-muted);
    line-height: 1.5;
  }

  .dot-list li::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0.6em;
    width: 6px;
    height: 6px;
    background: var(--clr-border-strong);
    border-radius: 50%;
  }

  .financial-dashboard {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
    background: white;
    padding: 1.5rem;
    border-radius: var(--radius-md);
    border: 1px solid var(--clr-border);
  }

  .fin-stat {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .fin-stat.positive .number {
    color: var(--clr-accent-dark);
  }

  .value-row {
    display: flex;
    align-items: baseline;
    gap: 0.25rem;
  }

  .unit {
    font-size: 0.875rem;
    font-weight: 700;
    color: var(--clr-text-muted);
  }

  .number {
    font-size: 1.5rem;
    font-weight: 800;
    color: var(--clr-primary);
  }

  .range {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-text-muted);
  }

  .loading-container {
    padding: 5rem 2rem;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
  }

  .ai-loader {
    position: relative;
    width: 60px;
    height: 60px;
  }

  .core {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 20px;
    height: 20px;
    background: var(--clr-accent);
    border-radius: 50%;
    box-shadow: 0 0 20px var(--clr-accent);
  }

  .orbit {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 2px solid var(--clr-accent-light);
    border-radius: 50%;
    border-top-color: var(--clr-accent);
    animation: spin 1.5s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .right-panels {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .sidebar-card {
    padding: 1.75rem;
  }

  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
  }

  .btn-icon-only {
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--clr-text-muted);
  }

  .btn-icon-only:hover {
    color: var(--clr-primary);
    border-color: var(--clr-border-strong);
  }

  .profile-summary {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2rem;
  }

  .profile-avatar {
    width: 48px;
    height: 48px;
    background: var(--clr-primary);
    color: white;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
  }

  .profile-info {
    display: flex;
    flex-direction: column;
  }

  .profile-name {
    font-weight: 700;
    color: var(--clr-primary);
  }

  .profile-id {
    font-size: 0.75rem;
    color: var(--clr-text-muted);
  }

  .data-list {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .data-item {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
  }

  .data-label {
    font-size: 0.625rem;
    font-weight: 800;
    text-transform: uppercase;
    color: var(--clr-text-muted);
    letter-spacing: 0.05em;
  }

  .data-value {
    font-size: 0.875rem;
    font-weight: 600;
    line-height: 1.5;
  }

  .data-value-small {
    font-size: 0.8125rem;
    color: var(--clr-text-muted);
    line-height: 1.4;
  }

  .data-divider {
    height: 1px;
    background: var(--clr-border);
    margin: 0.5rem 0;
  }

  .interests-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-top: 0.25rem;
  }

  .interest-badge {
    background: var(--clr-bg-alt);
    color: var(--clr-primary);
    padding: 0.25rem 0.625rem;
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: 700;
    border: 1px solid var(--clr-border);
  }

  .modal-checkbox-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    margin-top: 0.5rem;
  }

  .modal-checkbox {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
  }

  .modal-checkbox input {
    width: 1.25rem;
    height: 1.25rem;
    cursor: pointer;
    accent-color: var(--clr-accent);
  }

  .sidebar-card.ai-accent {
    background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
    border-color: var(--clr-accent-light);
  }

  .ai-sparkle {
    color: var(--clr-accent-dark) !important;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .ai-hint-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .ai-hint-list li {
    font-size: 0.875rem;
    color: var(--clr-text);
    padding-left: 1.5rem;
    position: relative;
    line-height: 1.4;
    font-weight: 500;
  }

  .ai-hint-list li::before {
    content: "→";
    position: absolute;
    left: 0;
    color: var(--clr-accent);
    font-weight: 800;
  }

  .empty-state {
    text-align: center;
    padding: 8rem 2rem;
    background: white;
    border-radius: var(--radius-lg);
    border: 2px dashed var(--clr-border);
  }

  .empty-illustration {
    font-size: 4rem;
    margin-bottom: 2rem;
  }

  /* Modals - Keeping existing modal logic but improving visuals */
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(15, 23, 42, 0.4);
    backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
  }

  .modal-card {
    width: 100%;
    max-width: 650px;
    max-height: 85vh;
    overflow-y: auto;
    padding: 3rem;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
  }

  .modal-header {
    margin-bottom: 2.5rem;
  }

  .modal-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }

  .modal-full {
    grid-column: span 2;
  }

  .modal-actions {
    margin-top: 3rem;
    display: flex;
    gap: 1rem;
  }

  .btn-modal-cancel {
    flex: 1;
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    padding: 1rem;
    border-radius: var(--radius-md);
    font-weight: 600;
  }

  .btn-modal-save {
    flex: 2;
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 1rem;
    border-radius: var(--radius-md);
    font-weight: 700;
  }

  @media (max-width: 1000px) {
    .main-body {
      grid-template-columns: 1fr;
    }
    .right-panels {
      display: grid;
      grid-template-columns: 1fr 1fr;
    }
  }

  @media (max-width: 768px) {
    .right-panels {
      grid-template-columns: 1fr;
    }
    .summary-highlight, .details-grid, .financial-dashboard {
      grid-template-columns: 1fr;
    }
    .main-header-title-row {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
