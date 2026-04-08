<script lang="ts">
  import type { Customer } from './types';

  let firstName = '';
  let lastName = '';
  let birthDate = '';
  let zip = '';
  let street = '';
  let houseNumber = '';

  let errors: Record<string, string> = {};
  let successMessage = '';

  function validate() {
    errors = {};
    if (!firstName.trim()) errors.firstName = 'Vorname ist erforderlich';
    if (!lastName.trim()) errors.lastName = 'Nachname ist erforderlich';
    if (!birthDate) errors.birthDate = 'Geburtsdatum ist erforderlich';
    if (!zip.trim()) errors.zip = 'PLZ ist erforderlich';
    if (!street.trim()) errors.street = 'Straße ist erforderlich';
    if (!houseNumber.trim()) errors.houseNumber = 'Hausnummer ist erforderlich';

    return Object.keys(errors).length === 0;
  }

  function handleSave() {
    successMessage = '';
    if (validate()) {
      const newCustomer: Customer = {
        firstName,
        lastName,
        birthDate,
        address: {
          zip,
          street,
          houseNumber
        }
      };

      // Mock-Save
      console.log('Speichere Kunde:', newCustomer);
      successMessage = 'Kunde erfolgreich gespeichert! (Mock)';
      
      // Felder leeren nach Speichern (optional für MVP, hier gemacht für UX)
      firstName = '';
      lastName = '';
      birthDate = '';
      zip = '';
      street = '';
      houseNumber = '';
    }
  }
</script>

<main class="container">
  <div class="form-card card">
    <div class="form-header">
      <h1>Neuen Kunden anlegen</h1>
      <p class="subtitle">Trage die Basisdaten deines neuen Kunden ein.</p>
    </div>

    <form onsubmit={(e) => { e.preventDefault(); handleSave(); }}>
      <div class="form-grid">
        <div class="form-group">
          <label for="firstName">Vorname *</label>
          <input 
            type="text" 
            id="firstName" 
            bind:value={firstName} 
            placeholder="z.B. Max"
            aria-invalid={!!errors.firstName}
            class:error={!!errors.firstName}
          />
          {#if errors.firstName}<span class="error-text">{errors.firstName}</span>{/if}
        </div>

        <div class="form-group">
          <label for="lastName">Nachname *</label>
          <input 
            type="text" 
            id="lastName" 
            bind:value={lastName} 
            placeholder="z.B. Mustermann"
            aria-invalid={!!errors.lastName}
            class:error={!!errors.lastName}
          />
          {#if errors.lastName}<span class="error-text">{errors.lastName}</span>{/if}
        </div>

        <div class="form-group full-width">
          <label for="birthDate">Geburtsdatum *</label>
          <input 
            type="date" 
            id="birthDate" 
            bind:value={birthDate} 
            aria-invalid={!!errors.birthDate}
            class:error={!!errors.birthDate}
          />
          {#if errors.birthDate}<span class="error-text">{errors.birthDate}</span>{/if}
        </div>

        <div class="address-section full-width">
          <h2 class="section-title">Adresse</h2>
          
          <div class="form-group">
            <label for="street">Straße *</label>
            <input 
              type="text" 
              id="street" 
              bind:value={street} 
              placeholder="Hauptstraße"
              aria-invalid={!!errors.street}
              class:error={!!errors.street}
            />
            {#if errors.street}<span class="error-text">{errors.street}</span>{/if}
          </div>

          <div class="row">
            <div class="form-group">
              <label for="houseNumber">Hausnummer *</label>
              <input 
                type="text" 
                id="houseNumber" 
                bind:value={houseNumber} 
                placeholder="10a"
                aria-invalid={!!errors.houseNumber}
                class:error={!!errors.houseNumber}
              />
              {#if errors.houseNumber}<span class="error-text">{errors.houseNumber}</span>{/if}
            </div>

            <div class="form-group">
              <label for="zip">PLZ *</label>
              <input 
                type="text" 
                id="zip" 
                bind:value={zip} 
                placeholder="12345"
                aria-invalid={!!errors.zip}
                class:error={!!errors.zip}
              />
              {#if errors.zip}<span class="error-text">{errors.zip}</span>{/if}
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-save">Kunde speichern</button>
      </div>

      {#if successMessage}
        <div class="alert alert-success" role="alert">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
          {successMessage}
        </div>
      {/if}
    </form>
  </div>
</main>

<style>
  .container {
    max-width: 700px;
    margin: 0 auto;
    padding: 1rem 0;
  }

  .form-card {
    padding: 2.5rem;
  }

  .form-header {
    margin-bottom: 2rem;
  }

  h1 {
    font-size: 1.75rem;
    margin-bottom: 0.5rem;
  }

  .subtitle {
    color: var(--clr-text-light);
    font-size: 1rem;
  }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }

  .full-width {
    grid-column: span 2;
  }

  .section-title {
    font-size: 1.125rem;
    margin: 1rem 0 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--clr-border);
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-primary);
  }

  input {
    padding: 0.75rem 1rem;
    border: 1px solid var(--clr-border);
    border-radius: 8px;
    font-size: 1rem;
    transition: all 0.2s;
    background-color: var(--clr-bg-alt);
  }

  input:focus {
    outline: none;
    border-color: var(--clr-accent);
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
    background-color: white;
  }

  input.error {
    border-color: var(--clr-error);
    background-color: #fffafb;
  }

  .error-text {
    color: var(--clr-error);
    font-size: 0.75rem;
    font-weight: 500;
  }

  .row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }

  .form-actions {
    margin-top: 2.5rem;
  }

  .btn-save {
    background-color: var(--clr-primary);
    color: white;
    border: none;
    padding: 1rem;
    border-radius: 10px;
    font-size: 1rem;
    font-weight: 600;
    width: 100%;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-save:hover {
    background-color: #1e293b;
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
  }

  .alert {
    margin-top: 1.5rem;
    padding: 1rem;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-weight: 500;
  }

  .alert-success {
    background-color: #ecfdf5;
    color: var(--clr-success);
    border: 1px solid #d1fae5;
  }

  @media (max-width: 600px) {
    .form-grid, .row {
      grid-template-columns: 1fr;
    }
    .full-width {
      grid-column: span 1;
    }
    .form-card {
      padding: 1.5rem;
    }
  }
</style>
