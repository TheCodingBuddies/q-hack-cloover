<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import type { Customer } from './types';

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
      }
    } catch (err) {
      error = 'Failed to load customer data';
    } finally {
      isLoading = false;
    }
  });

  // Edit customer
  let editingCustomer = $state(false);
  let editCustomerForm = $state({ firstName: '', lastName: '', birthDate: '', street: '', houseNumber: '', zip: '', city: '', customerProfile: '', energyConsumption: '', existingSystems: '', financialProfile: '', conversationHistory: '' });

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
    };
    editingCustomer = false;
  }

  // Edit project
  let editingProject = $state(false);
  let editProjectForm = $state({ name: '', location: '' });

  function openEditProject() {
    if (!project) return;
    editProjectForm = { name: project.name, location: project.location };
    editingProject = true;
  }

  function saveEditProject() {
    if (!project) return;
    project.name = editProjectForm.name;
    project.location = editProjectForm.location;
    editingProject = false;
  }

  function onModalKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape') {
      editingCustomer = false;
      editingProject = false;
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
            <span class="project-tag">Project</span>
            <div class="main-header-title-row">
              <h2>{project.name}</h2>
              <div class="header-actions">
                <button class="btn-present" onclick={() => {
                  window.history.pushState({}, '', `/customer/${customerId}/offer`);
                  window.dispatchEvent(new PopStateEvent('popstate'));
                }}>Show offer</button>
              </div>
            </div>
          </div>
        </div>

        <div class="main-body">
          <!-- Offer preview -->
          <div class="offer-preview card">
            <div class="panel-title">Offer preview</div>
            <div class="offer-text">
              {#each project.offerPreview.split('\n') as line}
                {#if line.startsWith('**') && line.endsWith('**')}
                  <strong>{line.slice(2, -2)}</strong><br/>
                {:else if line.startsWith('- ')}
                  <li>{line.slice(2)}</li>
                {:else if line.trim() === ''}
                  <br/>
                {:else}
                  <span>{line}</span><br/>
                {/if}
              {/each}
            </div>
          </div>

          <!-- Right panels -->
          <div class="right-panels">
            <div class="info-card card">
              <div class="card-title-row">
                <div class="panel-title">Customer info</div>
                <button class="btn-edit" aria-label="Edit customer info" onclick={openEditCustomer}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
              </div>
              <div class="info-row">
                <span class="info-label">Name</span>
                <span>{customer.firstName} {customer.lastName}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Date of birth</span>
                <span>{new Date(customer.birthDate).toLocaleDateString()}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Address</span>
                {#if customer.address}
                  <span>{customer.address.street} {customer.address.houseNumber}, {customer.address.zip} {customer.address.city}</span>
                {:else}
                  <span class="no-address-text">No address provided</span>
                {/if}
              </div>

              {#if customer.details}
                <hr class="divider" />
                {#if customer.details.customerProfile}
                  <div class="info-row">
                    <span class="info-label">Profile</span>
                    <span class="info-value-block">{customer.details.customerProfile}</span>
                  </div>
                {/if}
                {#if customer.details.energyConsumption}
                  <div class="info-row">
                    <span class="info-label">Energy</span>
                    <span class="info-value-block">{customer.details.energyConsumption}</span>
                  </div>
                {/if}
                {#if customer.details.existingSystems}
                  <div class="info-row">
                    <span class="info-label">Systems</span>
                    <span class="info-value-block">{customer.details.existingSystems}</span>
                  </div>
                {/if}
                {#if customer.details.financialProfile}
                  <div class="info-row">
                    <span class="info-label">Finance</span>
                    <span class="info-value-block">{customer.details.financialProfile}</span>
                  </div>
                {/if}
                {#if customer.details.conversationHistory}
                  <div class="info-row">
                    <span class="info-label">History</span>
                    <span class="info-value-block">{customer.details.conversationHistory}</span>
                  </div>
                {/if}
              {/if}
            </div>

            <div class="info-card card">
              <div class="card-title-row">
                <div class="panel-title">Project info</div>
                <button class="btn-edit" aria-label="Edit project info" onclick={openEditProject}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
              </div>
              <div class="info-row">
                <span class="info-label">Project name</span>
                <span>{project.name}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Location</span>
                <span>{project.location}</span>
              </div>
            </div>

            <div class="info-card card">
              <div class="panel-title ai-title">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
                AI hints
              </div>
              <ul class="ai-list">
                {#each project.aiHints as hint}
                  <li>{hint}</li>
                {/each}
              </ul>
            </div>
          </div>
        </div>
      {:else}
        <div class="empty-state">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 0 0-4 0v2"/></svg>
          <p>No project yet for {customer.firstName} {customer.lastName}</p>
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

{#if editingProject && project}
  <div class="modal-backdrop" role="presentation" onkeydown={onModalKeydown}>
    <div class="modal-card card" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="edit-project-title">
      <div class="modal-header">
        <h2 id="edit-project-title">Edit project info</h2>
        <p class="modal-subtitle">Update project details.</p>
      </div>
      <form onsubmit={(e) => { e.preventDefault(); saveEditProject(); }}>
        <div class="modal-form-group">
          <label for="ep-name">Project name *</label>
          <input id="ep-name" type="text" bind:value={editProjectForm.name} placeholder="Project name" />
        </div>
        <div class="modal-form-group">
          <label for="ep-location">Location *</label>
          <input id="ep-location" type="text" bind:value={editProjectForm.location} placeholder="Location" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" onclick={() => editingProject = false}>Cancel</button>
          <button type="submit" class="btn-modal-save">Save changes</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<style>
  .layout {
    display: block;
    max-width: 1200px;
    margin: 2rem auto;
    padding: 0 1rem;
  }

  .main {
    width: 100%;
    background: #f8fafc;
    overflow-y: auto;
    padding: 2.5rem;
  }

  .main-header {
    margin-bottom: 2.5rem;
  }

  .project-tag {
    display: inline-block;
    padding: 0.25rem 0.625rem;
    background: #e0f2fe;
    color: #0369a1;
    border-radius: 9999px;
    font-size: 0.7rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.025em;
    margin-bottom: 0.75rem;
  }

  .main-header-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.25rem;
    gap: 1rem;
  }

  .header-actions {
    display: flex;
    gap: 1rem;
    align-items: center;
  }

  .main-header h2 {
    font-size: 1.875rem;
    font-weight: 800;
    color: var(--clr-text);
  }

  .btn-present {
    background: white;
    border: 1px solid var(--clr-border);
    padding: 0.5rem 1rem;
    border-radius: 6px;
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-text);
    box-shadow: var(--shadow-sm);
  }

  .main-body {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 2rem;
    align-items: start;
  }

  .offer-preview {
    min-height: 500px;
    padding: 2rem;
  }

  .panel-title {
    font-size: 0.875rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--clr-text-light);
    margin-bottom: 1.25rem;
  }

  .offer-text {
    font-family: inherit;
    line-height: 1.6;
    color: var(--clr-text);
  }

  .offer-text strong {
    font-weight: 700;
    color: var(--clr-text);
  }

  .offer-text li {
    margin-left: 1.25rem;
    margin-bottom: 0.25rem;
  }

  .right-panels {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .info-card {
    padding: 1.5rem;
  }

  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1rem;
  }

  .card-title-row .panel-title {
    margin-bottom: 0;
  }

  .btn-edit {
    background: none;
    border: none;
    color: var(--clr-text-light);
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 4px;
    transition: background 0.15s, color 0.15s;
  }

  .btn-edit:hover {
    background: var(--clr-bg-alt);
    color: var(--clr-primary);
  }

  .info-row {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
    margin-bottom: 1rem;
  }

  .info-row:last-child {
    margin-bottom: 0;
  }

  .info-label {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--clr-text-light);
  }

  .info-row span:not(.info-label) {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--clr-text);
  }

  .info-value-block {
    display: block;
    font-size: 0.85rem !important;
    font-weight: 400 !important;
    color: var(--clr-text-light) !important;
    line-height: 1.4;
    white-space: pre-wrap;
  }

  .divider {
    border: 0;
    border-top: 1px solid var(--clr-border);
    margin: 1.25rem 0;
    opacity: 0.5;
  }

  .ai-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--clr-accent) !important;
  }

  .ai-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .ai-list li {
    font-size: 0.875rem;
    color: var(--clr-text);
    padding-left: 1.25rem;
    position: relative;
    line-height: 1.4;
  }

  .ai-list li::before {
    content: "•";
    position: absolute;
    left: 0;
    color: var(--clr-accent);
    font-weight: bold;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
    color: var(--clr-text-light);
  }

  .empty-state svg {
    margin-bottom: 1.5rem;
    opacity: 0.5;
  }

  /* Modals */
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(15, 23, 42, 0.6);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .modal-card {
    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
    padding: 2rem;
    animation: modal-enter 0.3s ease-out;
  }

  @keyframes modal-enter {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
  }

  .modal-header {
    margin-bottom: 1.5rem;
  }

  .modal-header h2 {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--clr-text);
    margin-bottom: 0.5rem;
  }

  .modal-subtitle {
    font-size: 0.875rem;
    color: var(--clr-text-light);
  }

  .modal-form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1.25rem;
  }

  .modal-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.25rem;
  }

  .modal-full {
    grid-column: span 2;
  }

  .modal-form-group label {
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--clr-text-light);
  }

  .modal-form-group input,
  .modal-form-group textarea {
    padding: 0.75rem;
    border: 1px solid var(--clr-border);
    border-radius: 6px;
    font-size: 0.9rem;
    font-family: inherit;
  }

  .modal-form-group textarea {
    min-height: 80px;
    resize: vertical;
  }

  .modal-divider {
    border: 0;
    border-top: 1px solid var(--clr-border);
    margin: 0.5rem 0;
  }

  .modal-actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
  }

  .btn-modal-cancel {
    flex: 1;
    background: white;
    border: 1px solid var(--clr-border);
    padding: 0.75rem;
    border-radius: 6px;
    font-weight: 600;
    color: var(--clr-text);
  }

  .btn-modal-save {
    flex: 2;
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 0.75rem;
    border-radius: 6px;
    font-weight: 600;
  }

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid var(--clr-border);
    border-top: 4px solid var(--clr-accent);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  .loading-full {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  @media (max-width: 1200px) {
    .main-body {
      grid-template-columns: 1fr;
    }
    .right-panels {
      order: -1;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }
  }

  @media (max-width: 768px) {
    .right-panels {
      grid-template-columns: 1fr;
    }
    .layout {
      margin: 0;
      height: auto;
    }
  }
</style>
