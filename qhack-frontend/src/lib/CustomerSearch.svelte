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
  <div class="search-box">
    <div class="input-wrapper">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="search-icon"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
      <input 
        type="text" 
        placeholder="Search customers by name or city..." 
        bind:value={searchQuery}
        oninput={onInput}
        aria-label="Search customers"
      />
      {#if isLoading}
        <div class="spinner small"></div>
      {:else if searchQuery}
        <button class="clear-btn" onclick={() => { searchQuery = ''; results = []; hasSearched = false; }}>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </button>
      {/if}
    </div>
  </div>

  {#if hasSearched && results.length === 0 && !isLoading}
    <div class="status-message no-results">
      <div class="empty-icon">🔍</div>
      <p>No customers found for <strong>"{searchQuery}"</strong>.</p>
    </div>
  {:else if results.length > 0}
    <div class="results-list">
      {#each results as customer}
        <a href="/customer/{customer.id}" class="customer-card" onclick={(e) => { e.preventDefault(); navigateToCustomer(customer.id); }}>
          <div class="customer-main">
            <div class="avatar">{customer.firstName[0]}{customer.lastName[0]}</div>
            <div class="customer-info">
              <div class="customer-name">{customer.firstName} {customer.lastName}</div>
              <div class="customer-meta">ID: {customer.id.substring(0, 8)}... • Born: {new Date(customer.birthDate).toLocaleDateString()}</div>
            </div>
          </div>
          <div class="customer-address">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
            <span>{customer.address?.city || 'Unknown City'}</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="arrow-icon"><polyline points="9 18 15 12 9 6"></polyline></svg>
          </div>
        </a>
      {/each}
    </div>
  {/if}
</div>

<style>
  .search-container {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    position: relative;
  }

  .search-box {
    background: var(--clr-bg-alt);
    border: 2px solid var(--clr-border);
    border-radius: var(--radius-lg);
    padding: 0.5rem 1.25rem;
    display: flex;
    align-items: center;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .search-box:focus-within {
    background: white;
    border-color: var(--clr-accent);
    box-shadow: 0 8px 30px rgba(16, 185, 129, 0.12);
    transform: translateY(-1px);
  }

  .input-wrapper {
    display: flex;
    align-items: center;
    gap: 1rem;
    width: 100%;
  }

  .search-icon {
    color: var(--clr-text-muted);
    flex-shrink: 0;
  }

  input {
    border: none;
    background: transparent;
    font-size: 1.125rem;
    width: 100%;
    outline: none;
    padding: 0.75rem 0;
    font-weight: 500;
    color: var(--clr-primary);
  }

  input::placeholder {
    color: var(--clr-text-muted);
    font-weight: 400;
  }

  .clear-btn {
    background: none;
    border: none;
    color: var(--clr-text-muted);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0.5rem;
    border-radius: 50%;
  }

  .clear-btn:hover {
    background: rgba(0,0,0,0.05);
    color: var(--clr-primary);
  }

  .status-message {
    text-align: center;
    padding: 3rem 2rem;
    color: var(--clr-text-muted);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
  }

  .empty-icon {
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
  }

  .spinner {
    width: 24px;
    height: 24px;
    border: 3px solid var(--clr-border);
    border-top-color: var(--clr-accent);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .spinner.small {
    width: 20px;
    height: 20px;
    border-width: 2.5px;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .results-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    max-height: 400px;
    overflow-y: auto;
    padding: 0.5rem;
    margin: 0 -0.5rem;
  }

  .customer-card {
    background: white;
    padding: 1rem 1.25rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-radius: var(--radius-md);
    border: 1px solid var(--clr-border);
    transition: all 0.2s;
    text-decoration: none;
    color: inherit;
    box-shadow: var(--shadow-sm);
  }

  .customer-card:hover {
    border-color: var(--clr-accent);
    transform: translateX(4px);
    box-shadow: var(--shadow-md);
    background: var(--clr-accent-light);
  }

  .customer-main {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .avatar {
    width: 40px;
    height: 40px;
    background: var(--clr-primary);
    color: white;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 0.875rem;
    letter-spacing: 0.05em;
  }

  .customer-info {
    display: flex;
    flex-direction: column;
  }

  .customer-name {
    font-weight: 700;
    font-size: 1rem;
    color: var(--clr-primary);
  }

  .customer-meta {
    font-size: 0.75rem;
    color: var(--clr-text-muted);
    font-weight: 500;
  }

  .customer-address {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.8125rem;
    color: var(--clr-text-muted);
    font-weight: 600;
  }

  .arrow-icon {
    color: var(--clr-border-strong);
    margin-left: 0.5rem;
    transition: transform 0.2s;
  }

  .customer-card:hover .arrow-icon {
    color: var(--clr-accent-dark);
    transform: translateX(3px);
  }

  @media (max-width: 600px) {
    .customer-card {
      padding: 0.875rem;
    }
    .customer-meta {
      display: none;
    }
    .customer-address span {
      display: none;
    }
  }
</style>
