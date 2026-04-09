<script lang="ts">
  import AddCustomer from './lib/AddCustomer.svelte';
  import CustomerSearch from './lib/CustomerSearch.svelte';
  import AllCustomers from './lib/AllCustomers.svelte';
  import CustomerDetails from './lib/CustomerDetails.svelte';
  import OfferDetails from './lib/OfferDetails.svelte';
  import { onMount } from 'svelte';

  let currentPath = '';

  // Hilfsfunktion zum Extrahieren der Kunden-ID aus dem Pfad
  function getCustomerId(path: string): string | null {
    const match = path.match(/^\/customer\/([^\/]+)$/);
    return match ? match[1] : null;
  }

  function getOfferCustomerId(path: string): string | null {
    const match = path.match(/^\/customer\/([^\/]+)\/offer$/);
    return match ? match[1] : null;
  }

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
      <div class="logo-icon">C</div>
      <span class="logo-text">Cloover</span><span class="logo-dot">.</span>
    </div>
    <div class="nav-links">
      <button class="nav-link" class:active={currentPath === '/'} onclick={() => navigate('/')}>Dashboard</button>
      <button class="nav-link" class:active={currentPath === '/all-customer'} onclick={() => navigate('/all-customer')}>Customers</button>
      <button class="btn-primary" onclick={() => navigate('/add-customer')}>
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        <span>Add Customer</span>
      </button>
    </div>
  </div>
</nav>

<main>
  {#if currentPath === '/add-customer'}
    <div class="page-container">
      <AddCustomer/>
    </div>
  {:else if currentPath === '/all-customer'}
    <div class="page-container">
      <AllCustomers/>
    </div>
  {:else if getOfferCustomerId(currentPath)}
    <OfferDetails customerId={getOfferCustomerId(currentPath) || ''} />
  {:else if getCustomerId(currentPath)}
    <div class="page-container">
      <CustomerDetails customerId={getCustomerId(currentPath) || ''} />
    </div>
  {:else}
    <section id="hero">
      <div class="hero-bg-glow"></div>
      <div class="hero-content">
        <span class="badge badge-accent">AI-Powered Sales MVP</span>
        <h1>Your <span class="text-gradient">Sales Coach</span> for renewable energy solutions</h1>
        <p>Revolutionize your workflow. Manage customers and generate KI-optimized offers in seconds.</p>
        
        <div class="hero-search-card">
          <div class="search-header">
            <h3>Find or explore customers</h3>
          </div>
          <CustomerSearch />
          <div class="search-footer">
            <button class="btn-text" onclick={() => navigate('/all-customer')}>
              Browse all customers →
            </button>
          </div>
        </div>

        <div class="hero-actions">
          <button class="btn-primary btn-lg" onclick={() => navigate('/add-customer')}>
            Get Started: Create New Customer
          </button>
        </div>
      </div>
    </section>
  {/if}
</main>

<style>
  :global(body) {
    background-color: var(--clr-bg-alt);
    color: var(--clr-text);
  }

  nav {
    background: var(--clr-bg-glass);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--clr-border);
    padding: 0.75rem 2rem;
    position: sticky;
    top: 0;
    z-index: 1000;
  }

  .nav-container {
    max-width: 1300px;
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
    align-items: center;
    gap: 0.75rem;
    transition: transform 0.2s;
  }

  .logo:hover {
    transform: scale(1.02);
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, var(--clr-primary) 0%, var(--clr-accent) 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    font-size: 1.25rem;
    font-weight: 900;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  }

  .logo-text {
    color: var(--clr-primary);
    letter-spacing: -0.03em;
  }

  .logo-dot {
    color: var(--clr-accent);
  }

  .nav-links {
    display: flex;
    gap: 1rem;
    align-items: center;
  }

  .nav-link {
    background: none;
    border: none;
    color: var(--clr-text-muted);
    font-weight: 600;
    padding: 0.5rem 1rem;
    border-radius: var(--radius-md);
    font-size: 0.9375rem;
  }

  .nav-link:hover {
    color: var(--clr-primary);
    background: rgba(0,0,0,0.03);
  }

  .nav-link.active {
    color: var(--clr-primary);
    background: white;
    box-shadow: var(--shadow-sm);
  }

  main {
    min-height: calc(100vh - 70px);
    position: relative;
    overflow-x: hidden;
  }

  .page-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem;
  }

  #hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8rem 2rem;
    text-align: center;
    position: relative;
  }

  .hero-bg-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 60vw;
    height: 60vw;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.08) 0%, rgba(255,255,255,0) 70%);
    pointer-events: none;
    z-index: -1;
  }

  .hero-content {
    max-width: 900px;
    width: 100%;
    z-index: 1;
  }

  .hero-search-card {
    background: white;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
    padding: 2.5rem;
    margin: 3rem 0;
    border: 1px solid var(--clr-border);
    text-align: left;
  }

  .search-header h3 {
    margin-bottom: 1.5rem;
    font-size: 1.25rem;
    color: var(--clr-text-muted);
  }

  .search-footer {
    margin-top: 1.5rem;
    display: flex;
    justify-content: center;
  }

  .btn-text {
    background: none;
    border: none;
    color: var(--clr-accent-dark);
    font-weight: 600;
    font-size: 0.9375rem;
  }

  .btn-text:hover {
    text-decoration: underline;
    opacity: 0.8;
  }

  h1 {
    font-size: 4rem;
    line-height: 1.1;
    margin-bottom: 1.5rem;
    letter-spacing: -0.04em;
  }

  p {
    font-size: 1.35rem;
    color: var(--clr-text-muted);
    margin-bottom: 3rem;
    line-height: 1.6;
    max-width: 700px;
    margin-left: auto;
    margin-right: auto;
  }

  .hero-actions {
    display: flex;
    gap: 1.5rem;
    justify-content: center;
  }

  .btn-primary {
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: var(--radius-md);
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.15);
  }

  .btn-primary:hover {
    background: var(--clr-primary-light);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(15, 23, 42, 0.2);
  }

  .btn-lg {
    padding: 1.25rem 2.5rem;
    font-size: 1.125rem;
    border-radius: var(--radius-lg);
  }

  @media (max-width: 768px) {
    h1 {
      font-size: 2.75rem;
    }
    .hero-actions {
      flex-direction: column;
    }
    .page-container {
      padding: 1.5rem 1rem;
    }
  }
</style>

