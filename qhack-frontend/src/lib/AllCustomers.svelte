<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import type { Customer } from './types';

  let customers: Customer[] = $state([]);
  let isLoading = $state(true);
  let error = $state<string | null>(null);
  let searchTerm = $state('');

  onMount(async () => {
    try {
      customers = await customerService.getAllCustomers();
    } catch (err) {
      error = 'Failed to load customers. Please try again later.';
      console.error(err);
    } finally {
      isLoading = false;
    }
  });

  function navigateToCustomer(id: string | undefined) {
    if (!id) return;
    window.history.pushState({}, '', `/customer/${id}`);
    window.dispatchEvent(new PopStateEvent('popstate'));
  }

  let filteredCustomers = $derived(
    customers.filter(c => 
      `${c.firstName} ${c.lastName}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (c.address?.city.toLowerCase().includes(searchTerm.toLowerCase()))
    )
  );
</script>

<div class="overview-container">
  <header class="overview-header">
    <div class="header-content">
      <h1>Customer Overview</h1>
      <p class="subtitle">Manage your customer relationships and project progress at a glance.</p>
    </div>
    
    <div class="search-wrapper">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input 
        type="text" 
        placeholder="Search by name or city..." 
        bind:value={searchTerm}
      />
    </div>
  </header>

  {#if isLoading}
    <div class="loading-state">
      <div class="spinner"></div>
      <p>Loading customers...</p>
    </div>
  {:else if error}
    <div class="empty-state-container card">
      <p class="error-message">{error}</p>
      <button class="primary-button" onclick={() => window.location.reload()}>Try Again</button>
    </div>
  {:else if filteredCustomers.length === 0}
    <div class="empty-state-container card">
      <p>No customers found matching your search.</p>
    </div>
  {:else}
    <div class="customer-grid">
      {#each filteredCustomers as customer}
        <button class="customer-card card" onclick={() => navigateToCustomer(customer.id)}>
          <div class="card-header">
            <div class="avatar">
              {customer.firstName[0]}{customer.lastName[0]}
            </div>
            <div class="customer-meta">
              <h3>{customer.firstName} {customer.lastName}</h3>
              <div class="address-info">
                {#if customer.address}
                  <span class="street">{customer.address.street} {customer.address.houseNumber}</span>
                  <span class="city">{customer.address.zip} {customer.address.city}</span>
                {:else}
                  <span class="city">No address provided</span>
                {/if}
              </div>
            </div>
          </div>
          
          <div class="card-footer-action">
            <span class="view-details-text">View Details</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </div>
        </button>
      {/each}
    </div>
  {/if}
</div>

<style>
  .overview-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 3rem;
    gap: 2rem;
  }

  .header-content h1 {
    font-size: 2.25rem;
    font-weight: 800;
    color: var(--clr-text);
    margin: 0 0 0.5rem 0;
    letter-spacing: -0.02em;
  }

  .subtitle {
    color: var(--clr-text-light);
    font-size: 1.1rem;
    margin: 0;
  }

  .search-wrapper {
    position: relative;
    width: 100%;
    max-width: 400px;
  }

  .search-wrapper svg {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: var(--clr-text-light);
    pointer-events: none;
  }

  .search-wrapper input {
    width: 100%;
    padding: 0.875rem 1rem 0.875rem 3rem;
    border: 1px solid var(--clr-border);
    border-radius: 12px;
    font-size: 1rem;
    background: white;
    transition: all 0.2s;
  }

  .search-wrapper input:focus {
    outline: none;
    border-color: var(--clr-primary);
    box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
  }

  .customer-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 1.5rem;
  }

  .customer-card {
    display: flex;
    flex-direction: column;
    text-align: left;
    padding: 1.5rem;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    background: white;
    border: none;
    width: 100%;
  }

  .customer-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px -10px rgba(15, 23, 42, 0.15);
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: var(--clr-bg-alt);
    color: var(--clr-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.1rem;
  }

  .customer-meta h3 {
    margin: 0;
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--clr-text);
  }

  .city {
    font-size: 0.85rem;
    color: var(--clr-text-light);
    display: block;
  }

  .street {
    font-size: 0.85rem;
    color: var(--clr-text-light);
    display: block;
    margin-bottom: 0.125rem;
  }

  .address-info {
    margin-top: 0.25rem;
  }

  .card-footer-action {
    margin-top: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 1rem;
    border-top: 1px solid var(--clr-bg-alt);
    color: var(--clr-primary);
    font-weight: 600;
    font-size: 0.875rem;
  }

  .view-details-text {
    opacity: 0.8;
    transition: opacity 0.2s;
  }

  .customer-card:hover .view-details-text {
    opacity: 1;
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 5rem;
    color: var(--clr-text-light);
  }

  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--clr-bg-alt);
    border-top-color: var(--clr-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .empty-state-container {
    padding: 3rem;
    text-align: center;
    color: var(--clr-text-light);
  }

  @media (max-width: 768px) {
    .overview-header {
      flex-direction: column;
      align-items: flex-start;
      margin-bottom: 2rem;
    }
    
    .search-wrapper {
      max-width: none;
    }
  }
</style>
