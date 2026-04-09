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
      <div class="header-badge">
        <span class="badge badge-accent">Portfolio Management</span>
      </div>
      <h1>Customer Overview</h1>
      <p class="subtitle">Manage your customer relationships and project progress at a glance.</p>
    </div>
    
    <div class="search-wrapper">
      <div class="search-inner">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="search-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input 
          type="text" 
          placeholder="Filter by name, city or ZIP..." 
          bind:value={searchTerm}
        />
        {#if searchTerm}
          <button class="clear-btn" onclick={() => searchTerm = ''}>×</button>
        {/if}
      </div>
    </div>
  </header>

  {#if isLoading}
    <div class="loading-state">
      <div class="spinner"></div>
      <p>Synchronizing with database...</p>
    </div>
  {:else if error}
    <div class="empty-state-container card">
      <div class="empty-icon error">⚠️</div>
      <p class="error-message">{error}</p>
      <button class="btn-primary" onclick={() => window.location.reload()}>Try Again</button>
    </div>
  {:else if filteredCustomers.length === 0}
    <div class="empty-state-container card">
      <div class="empty-icon">📂</div>
      <h3>No customers found</h3>
      <p>Try adjusting your search criteria or add a new customer.</p>
      <button class="btn-outline" onclick={() => searchTerm = ''} style="margin-top: 1rem;">Clear Search</button>
    </div>
  {:else}
    <div class="customer-grid">
      {#each filteredCustomers as customer}
        <button class="customer-card card" onclick={() => navigateToCustomer(customer.id)}>
          <div class="card-body">
            <div class="avatar-group">
              <div class="avatar">
                {customer.firstName[0]}{customer.lastName[0]}
              </div>
              <div class="status-indicator active"></div>
            </div>
            <div class="customer-meta">
              <h3>{customer.firstName} {customer.lastName}</h3>
              <div class="address-info">
                {#if customer.address}
                  <span class="street">{customer.address.street} {customer.address.houseNumber}</span>
                  <div class="city-line">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                    <span class="city">{customer.address.zip} {customer.address.city}</span>
                  </div>
                {:else}
                  <span class="city">No address provided</span>
                {/if}
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <div class="tags">
              <span class="tag">Lead</span>
              <span class="tag secondary">Solar PV</span>
            </div>
            <div class="action-link">
              <span>Analysis</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </div>
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
  }

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 4rem;
    gap: 3rem;
  }

  .header-badge {
    margin-bottom: 1rem;
  }

  .header-content h1 {
    font-size: 2.75rem;
    font-weight: 800;
    color: var(--clr-primary);
    margin: 0 0 0.5rem 0;
    letter-spacing: -0.04em;
  }

  .subtitle {
    color: var(--clr-text-muted);
    font-size: 1.125rem;
    margin: 0;
    max-width: 500px;
    line-height: 1.5;
  }

  .search-wrapper {
    width: 100%;
    max-width: 450px;
  }

  .search-inner {
    position: relative;
    display: flex;
    align-items: center;
  }

  .search-icon {
    position: absolute;
    left: 1.25rem;
    color: var(--clr-text-muted);
    pointer-events: none;
  }

  .search-inner input {
    width: 100%;
    padding: 1rem 1rem 1rem 3.5rem;
    border: 2px solid var(--clr-border);
    border-radius: var(--radius-lg);
    font-size: 1rem;
    background: white;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    font-weight: 500;
  }

  .search-inner input:focus {
    outline: none;
    border-color: var(--clr-accent);
    box-shadow: 0 8px 30px rgba(16, 185, 129, 0.1);
    transform: translateY(-2px);
  }

  .clear-btn {
    position: absolute;
    right: 1rem;
    background: var(--clr-bg-alt);
    border: none;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 1.25rem;
    color: var(--clr-text-muted);
  }

  .customer-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    gap: 2rem;
  }

  .customer-card {
    display: flex;
    flex-direction: column;
    text-align: left;
    padding: 0;
    cursor: pointer;
    background: white;
    border: 1px solid var(--clr-border);
    width: 100%;
    border-radius: var(--radius-lg);
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .customer-card:hover {
    transform: translateY(-8px);
    box-shadow: var(--shadow-xl);
    border-color: var(--clr-accent);
  }

  .card-body {
    padding: 2rem;
    display: flex;
    align-items: flex-start;
    gap: 1.25rem;
  }

  .avatar-group {
    position: relative;
    flex-shrink: 0;
  }

  .avatar {
    width: 60px;
    height: 60px;
    border-radius: 18px;
    background: var(--clr-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 1.25rem;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  }

  .status-indicator {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 16px;
    height: 16px;
    background: var(--clr-success);
    border: 3px solid white;
    border-radius: 50%;
  }

  .customer-meta h3 {
    margin: 0 0 0.5rem 0;
    font-size: 1.25rem;
    font-weight: 800;
    color: var(--clr-primary);
    letter-spacing: -0.02em;
  }

  .address-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .street {
    font-size: 0.875rem;
    color: var(--clr-text-muted);
    font-weight: 500;
  }

  .city-line {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    color: var(--clr-text-muted);
  }

  .city {
    font-size: 0.875rem;
    font-weight: 600;
  }

  .card-footer {
    padding: 1.25rem 2rem;
    background: var(--clr-bg-alt);
    border-top: 1px solid var(--clr-border);
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: auto;
  }

  .tags {
    display: flex;
    gap: 0.5rem;
  }

  .tag {
    font-size: 0.6875rem;
    font-weight: 700;
    padding: 0.25rem 0.625rem;
    border-radius: 6px;
    background: var(--clr-accent-light);
    color: var(--clr-accent-dark);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .tag.secondary {
    background: #e2e8f0;
    color: #475569;
  }

  .action-link {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--clr-primary);
    font-weight: 700;
    font-size: 0.875rem;
  }

  .action-link svg {
    transition: transform 0.2s;
  }

  .customer-card:hover .action-link svg {
    transform: translateX(4px);
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8rem 0;
    color: var(--clr-text-muted);
  }

  .spinner {
    width: 48px;
    height: 48px;
    border: 4px solid var(--clr-border);
    border-top-color: var(--clr-accent);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1.5rem;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .empty-state-container {
    padding: 5rem 2rem;
    text-align: center;
    max-width: 600px;
    margin: 2rem auto;
  }

  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1.5rem;
  }

  .empty-icon.error {
    color: var(--clr-error);
  }

  @media (max-width: 900px) {
    .overview-header {
      flex-direction: column;
      align-items: flex-start;
      margin-bottom: 3rem;
      gap: 1.5rem;
    }
    
    .search-wrapper {
      max-width: none;
    }

    .customer-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
