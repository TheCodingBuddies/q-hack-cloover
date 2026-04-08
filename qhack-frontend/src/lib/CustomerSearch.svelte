<script lang="ts">
  import { customerService } from './customerService';
  import type { Customer } from './types';

  let searchQuery = $state('');
  let results: Customer[] = $state([]);
  let isLoading = $state(false);
  let hasSearched = $state(false);

  async function handleSearch() {
    if (!searchQuery.trim()) {
      results = [];
      hasSearched = false;
      return;
    }

    isLoading = true;
    hasSearched = true;
    try {
      results = await customerService.searchCustomers(searchQuery);
    } catch (error) {
      console.error('Search failed:', error);
      results = [];
    } finally {
      isLoading = false;
    }
  }

  // Debounce-artige Suche beim Tippen (optional, aber gut für UX)
  let searchTimeout: ReturnType<typeof setTimeout>;
  function onInput() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      handleSearch();
    }, 300);
  }

  function navigateToCustomer(id: string) {
    if (!id) {
      console.error('Cannot navigate: No customer ID provided');
      return;
    }
    window.history.pushState({}, '', `/customer/${id}`);
    window.dispatchEvent(new PopStateEvent('popstate'));
  }
</script>

<div class="search-container">
  <div class="search-box card">
    <div class="input-wrapper">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="search-icon"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
      <input 
        type="text" 
        placeholder="Search customers by name or city..." 
        bind:value={searchQuery}
        oninput={onInput}
        aria-label="Search customers"
      />
    </div>
  </div>

  {#if isLoading}
    <div class="status-message">
      <div class="spinner"></div>
      Searching...
    </div>
  {:else if hasSearched && results.length === 0}
    <div class="status-message no-results card">
      No customers found for "{searchQuery}".
    </div>
  {:else if results.length > 0}
    <div class="results-list">
      {#each results as customer}
        <a href="/customer/{customer.id}" class="customer-card card" onclick={(e) => { e.preventDefault(); navigateToCustomer(customer.id); }}>
          <div class="customer-info">
            <div class="customer-name">{customer.firstName} {customer.lastName}</div>
            <div class="customer-meta">Born: {new Date(customer.birthDate).toLocaleDateString()}</div>
          </div>
          <div class="customer-address">
            {#if customer.address}
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
              <span>{customer.address.street} {customer.address.houseNumber}, {customer.address.zip} {customer.address.city}</span>
            {:else}
              <span class="no-address">No address provided</span>
            {/if}
          </div>
        </a>
      {/each}
    </div>
  {/if}
</div>

<style>
  .search-container {
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .search-box {
    padding: 0.75rem 1.5rem;
    display: flex;
    align-items: center;
  }

  .input-wrapper {
    display: flex;
    align-items: center;
    gap: 1rem;
    width: 100%;
  }

  .search-icon {
    color: var(--clr-text-light);
  }

  input {
    border: none;
    background: transparent;
    font-size: 1.125rem;
    width: 100%;
    outline: none;
    padding: 0.5rem 0;
  }

  .status-message {
    text-align: center;
    padding: 2rem;
    color: var(--clr-text-light);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .no-results {
    color: var(--clr-text);
  }

  .spinner {
    width: 24px;
    height: 24px;
    border: 3px solid var(--clr-border);
    border-top: 3px solid var(--clr-accent);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  .results-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .customer-card {
    padding: 1.25rem 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: transform 0.2s, box-shadow 0.2s;
    cursor: pointer;
    text-decoration: none;
    color: inherit;
  }

  .customer-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
    border-color: var(--clr-accent);
  }

  .customer-name {
    font-weight: 700;
    font-size: 1.125rem;
    color: var(--clr-primary);
  }

  .customer-meta {
    font-size: 0.875rem;
    color: var(--clr-text-light);
  }

  .customer-address {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: var(--clr-text-light);
    text-align: right;
  }

  @media (max-width: 600px) {
    .customer-card {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.75rem;
    }
    .customer-address {
      text-align: left;
    }
  }
</style>
