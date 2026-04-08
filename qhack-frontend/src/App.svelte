<script lang="ts">
  import AddCustomer from './lib/AddCustomer.svelte';
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
  <button onclick={() => navigate('/')}>Home</button>
  <button onclick={() => navigate('/add-customer')}>Kunde hinzufügen</button>
</nav>

{#if currentPath === '/add-customer'}
  <AddCustomer />
{:else}
  <section id="center">
    <h1>Willkommen beim KI-Auftragscoach</h1>
    <p>Hier entsteht die Zukunft der Auftragsverwaltung.</p>
    <button onclick={() => navigate('/add-customer')}>Jetzt ersten Kunden anlegen</button>
  </section>
{/if}

<style>
  nav {
    padding: 1rem;
    background: #f4f4f4;
    display: flex;
    gap: 1rem;
    border-bottom: 1px solid #ddd;
  }

  #center {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 50vh;
    text-align: center;
  }

  button {
    padding: 0.5rem 1rem;
    cursor: pointer;
  }
</style>

