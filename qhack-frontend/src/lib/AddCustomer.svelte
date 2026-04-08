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
  <h1>Neuen Kunden anlegen</h1>

  <form onsubmit={(e) => { e.preventDefault(); handleSave(); }}>
    <div class="form-group">
      <label for="firstName">Vorname *</label>
      <input 
        type="text" 
        id="firstName" 
        bind:value={firstName} 
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
        aria-invalid={!!errors.lastName}
        class:error={!!errors.lastName}
      />
      {#if errors.lastName}<span class="error-text">{errors.lastName}</span>{/if}
    </div>

    <div class="form-group">
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

    <fieldset>
      <legend>Adresse</legend>
      
      <div class="form-group">
        <label for="street">Straße *</label>
        <input 
          type="text" 
          id="street" 
          bind:value={street} 
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
            aria-invalid={!!errors.zip}
            class:error={!!errors.zip}
          />
          {#if errors.zip}<span class="error-text">{errors.zip}</span>{/if}
        </div>
      </div>
    </fieldset>

    <button type="submit">Speichern</button>

    {#if successMessage}
      <p class="success-text" role="alert">{successMessage}</p>
    {/if}
  </form>
</main>

<style>
  .container {
    max-width: 600px;
    margin: 2rem auto;
    padding: 1rem;
    font-family: sans-serif;
  }

  .form-group {
    margin-bottom: 1.2rem;
    display: flex;
    flex-direction: column;
  }

  label {
    margin-bottom: 0.3rem;
    font-weight: bold;
  }

  input {
    padding: 0.6rem;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 1rem;
  }

  input.error {
    border-color: #d32f2f;
    background-color: #fff8f8;
  }

  .error-text {
    color: #d32f2f;
    font-size: 0.85rem;
    margin-top: 0.2rem;
  }

  .success-text {
    color: #2e7d32;
    margin-top: 1rem;
    font-weight: bold;
  }

  fieldset {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 1rem;
    margin-bottom: 1.5rem;
  }

  legend {
    padding: 0 0.5rem;
    font-weight: bold;
  }

  .row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  button {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 0.8rem 1.5rem;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    width: 100%;
  }

  button:hover {
    background-color: #0056b3;
  }

  @media (max-width: 480px) {
    .row {
      grid-template-columns: 1fr;
    }
  }
</style>
