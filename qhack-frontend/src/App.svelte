<script lang="ts">
  import AddCustomer from './lib/AddCustomer.svelte';
  import CustomerSearch from './lib/CustomerSearch.svelte';
  import { onMount } from 'svelte';

  let currentPath = '';

  onMount(() => {
    currentPath = window.location.pathname;
    
    // Einfacher Listener für Browser-Navigation (Back/Forward)
    const handlePopState = () => {
      currentPath = window.location.pathname;
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  });

  // Hilfsfunktion zum Navigieren ohne Page-Reload
  function navigate(path: string) {
    window.history.pushState({}, '', path);
    currentPath = path;
  }
</script>

<nav>
  <div class="nav-container">
    <div class="logo" onclick={() => navigate('/')} role="button" tabindex="0" onkeydown={(e) => e.key === 'Enter' && navigate('/')}>
      <span class="logo-text">Cloover</span><span class="logo-dot">.</span>
    </div>
    <div class="nav-links">
      <button class="nav-link" class:active={currentPath === '/'} onclick={() => navigate('/')}>Home</button>
      <button class="btn-primary" onclick={() => navigate('/add-customer')}>Add customer</button>
    </div>
  </div>
</nav>

<main>
  {#if currentPath === '/add-customer'}
    <div class="page-container">
      <AddCustomer/>
    </div>
  {:else}
    <section id="hero">
      <div class="hero-content">
        <span class="badge">MVP Version</span>
        <h1>The future of <span class="text-gradient">Order Management</span></h1>
        <p>Manage your customers and orders with modern AI support. Fast, simple, and efficient.</p>
        
        <div class="hero-search">
          <CustomerSearch />
        </div>

        <div class="hero-actions">
          <button class="btn-primary btn-lg" onclick={() => navigate('/add-customer')}>
            Create new customer
          </button>
        </div>
      </div>
    </section>
  {/if}
</main>

<style>
  :global(body) {
    background-color: var(--clr-bg-alt);
  }

  nav {
    background: white;
    border-bottom: 1px solid var(--clr-border);
    padding: 1rem 2rem;
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .nav-container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    font-size: 1.5rem;
    font-weight: 800;
    cursor: pointer;
    display: flex;
    align-items: baseline;
  }

  .logo-text {
    color: var(--clr-primary);
  }

  .logo-dot {
    color: var(--clr-accent);
  }

  .nav-links {
    display: flex;
    gap: 1.5rem;
    align-items: center;
  }

  .nav-link {
    background: none;
    border: none;
    color: var(--clr-text-light);
    font-weight: 500;
    padding: 0.5rem;
  }

  .nav-link:hover, .nav-link.active {
    color: var(--clr-primary);
  }

  main {
    min-height: calc(100vh - 70px);
  }

  .page-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  #hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 6rem 2rem;
    text-align: center;
  }

  .hero-content {
    max-width: 800px;
    width: 100%;
  }

  .hero-search {
    margin: 2rem 0 3rem;
    width: 100%;
  }

  .badge {
    background: var(--clr-accent);
    color: white;
    padding: 0.25rem 0.75rem;
    border-radius: 99px;
    font-size: 0.875rem;
    font-weight: 600;
    margin-bottom: 1.5rem;
    display: inline-block;
  }

  h1 {
    font-size: 3.5rem;
    line-height: 1.1;
    margin-bottom: 1.5rem;
    letter-spacing: -0.02em;
  }

  .text-gradient {
    background: linear-gradient(90deg, var(--clr-primary), var(--clr-accent));
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  p {
    font-size: 1.25rem;
    color: var(--clr-text-light);
    margin-bottom: 2.5rem;
    line-height: 1.6;
  }

  .hero-actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
  }

  .btn-primary {
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: var(--radius-md);
    font-weight: 600;
  }

  .btn-primary:hover {
    background: #1e293b;
    transform: translateY(-1px);
  }

  .btn-lg {
    padding: 1rem 2rem;
    font-size: 1.125rem;
  }

  @media (max-width: 768px) {
    h1 {
      font-size: 2.5rem;
    }
    .hero-actions {
      flex-direction: column;
    }
  }
</style>

