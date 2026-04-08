<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import type { Customer } from './types';

  interface Props {
    customerId: string;
  }

  let { customerId }: Props = $props();
  
  let customer: Customer | null = $state(null);
  let isLoading = $state(true);
  let isSaving = $state(false);
  let error: string | null = $state(null);
  let successMessage = $state('');

  onMount(async () => {
    try {
      customer = await customerService.getCustomerById(customerId);
      if (!customer) {
        error = 'Customer not found';
      }
    } catch (err) {
      error = 'Failed to load customer data';
    } finally {
      isLoading = false;
    }
  });

  async function handleSave() {
    if (!customer) return;
    
    isSaving = true;
    successMessage = '';
    error = null;

    try {
      // In einem echten Backend würden wir hier eine updateCustomer Methode im Service haben.
      // Für den MVP Mock loggen wir es einfach und simulieren Erfolg.
      console.log('Updating customer:', customer);
      
      await new Promise(resolve => setTimeout(resolve, 500));
      successMessage = 'Customer updated successfully!';
    } catch (err) {
      error = 'Failed to update customer';
    } finally {
      isSaving = false;
    }
  }
</script>

<div class="detail-page">
  {#if isLoading}
    <div class="loading">
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
    <div class="split-layout">
      <!-- Linke Seite: Angebot Platzhalter -->
      <div class="offer-placeholder card">
        <div class="placeholder-content">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="placeholder-icon"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
          <h3>Your offer could be here</h3>
          <p>This area is reserved for future offer generation and management features.</p>
        </div>
      </div>

      <!-- Rechte Seite: Kundendaten editierbar -->
      <div class="customer-data card">
        <div class="card-header">
          <h2>Customer Details</h2>
          <p class="subtitle">View and edit customer information.</p>
        </div>

        <form onsubmit={(e) => { e.preventDefault(); handleSave(); }}>
          <div class="form-section">
            <h3 class="section-title">Personal Information</h3>
            <div class="form-grid">
              <div class="form-group">
                <label for="firstName">First name</label>
                <input type="text" id="firstName" bind:value={customer.firstName} />
              </div>
              <div class="form-group">
                <label for="lastName">Last name</label>
                <input type="text" id="lastName" bind:value={customer.lastName} />
              </div>
              <div class="form-group full-width">
                <label for="birthDate">Date of birth</label>
                <input type="date" id="birthDate" bind:value={customer.birthDate} />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">Address</h3>
            <div class="form-group">
              <label for="street">Street</label>
              <input type="text" id="street" bind:value={customer.address.street} />
            </div>
            <div class="row">
              <div class="form-group">
                <label for="houseNumber">No.</label>
                <input type="text" id="houseNumber" bind:value={customer.address.houseNumber} />
              </div>
              <div class="form-group">
                <label for="zip">ZIP</label>
                <input type="text" id="zip" bind:value={customer.address.zip} />
              </div>
              <div class="form-group">
                <label for="city">City</label>
                <input type="text" id="city" bind:value={customer.address.city} />
              </div>
            </div>
          </div>

          {#if customer.details}
            <div class="form-section">
              <h3 class="section-title">Additional Details</h3>
              <div class="form-group">
                <label for="customerProfile">Customer Profile</label>
                <input type="text" id="customerProfile" bind:value={customer.details.customerProfile} />
              </div>
              <div class="form-group">
                <label for="energyConsumption">Energy Consumption</label>
                <input type="text" id="energyConsumption" bind:value={customer.details.energyConsumption} />
              </div>
              <div class="form-group">
                <label for="existingSystems">Existing Systems</label>
                <input type="text" id="existingSystems" bind:value={customer.details.existingSystems} />
              </div>
              <div class="form-group">
                <label for="financialProfile">Financial Profile</label>
                <input type="text" id="financialProfile" bind:value={customer.details.financialProfile} />
              </div>
              <div class="form-group">
                <label for="conversationHistory">Conversation History</label>
                <textarea id="conversationHistory" bind:value={customer.details.conversationHistory} rows="3"></textarea>
              </div>
            </div>
          {/if}

          <div class="form-actions">
            <button type="submit" class="btn-save" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>

          {#if error}
            <div class="alert alert-error">{error}</div>
          {/if}
          {#if successMessage}
            <div class="alert alert-success">{successMessage}</div>
          {/if}
        </form>
      </div>
    </div>
  {/if}
</div>

<style>
  .detail-page {
    width: 100%;
  }

  .loading, .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem;
    text-align: center;
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

  .split-layout {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: start;
  }

  .offer-placeholder {
    height: 100%;
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--clr-bg-alt);
    border: 2px dashed var(--clr-border);
    text-align: center;
  }

  .placeholder-content {
    max-width: 300px;
    color: var(--clr-text-light);
  }

  .placeholder-icon {
    margin-bottom: 1.5rem;
    opacity: 0.5;
  }

  .customer-data {
    padding: 2.5rem;
  }

  .card-header {
    margin-bottom: 2rem;
  }

  .subtitle {
    color: var(--clr-text-light);
  }

  .section-title {
    font-size: 1.125rem;
    margin: 1.5rem 0 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid var(--clr-border);
    color: var(--clr-primary);
  }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.25rem;
  }

  .full-width {
    grid-column: span 2;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.4rem;
    margin-bottom: 1rem;
  }

  .row {
    display: grid;
    grid-template-columns: 1fr 2fr 3fr;
    gap: 1rem;
  }

  label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-text);
  }

  input, textarea {
    padding: 0.75rem;
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-md);
    font-size: 1rem;
    width: 100%;
    box-sizing: border-box;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  input:focus, textarea:focus {
    outline: none;
    border-color: var(--clr-accent);
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
  }

  .form-actions {
    margin-top: 2rem;
  }

  .btn-save {
    width: 100%;
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 1rem;
    border-radius: var(--radius-md);
    font-weight: 600;
    font-size: 1rem;
  }

  .btn-save:hover:not(:disabled) {
    background: #1e293b;
    transform: translateY(-1px);
  }

  .btn-save:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .alert {
    margin-top: 1.5rem;
    padding: 1rem;
    border-radius: var(--radius-md);
    font-size: 0.875rem;
  }

  .alert-error {
    background: #fef2f2;
    color: var(--clr-error);
    border: 1px solid #fee2e2;
  }

  .alert-success {
    background: #f0fdf4;
    color: var(--clr-success);
    border: 1px solid #dcfce7;
  }

  @media (max-width: 1024px) {
    .split-layout {
      grid-template-columns: 1fr;
    }
    .offer-placeholder {
      min-height: 200px;
      order: 2;
    }
  }
</style>
