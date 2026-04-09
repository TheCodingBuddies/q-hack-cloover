<script lang="ts">
  import type { CreateCustomerDTO } from './types';
  import { customerService } from './customerService';

  let firstName = '';
  let lastName = '';
  let birthDate = '';
  let zip = '';
  let city = '';
  let street = '';
  let houseNumber = '';

  // Optional fields
  let customerProfile = '';
  let energyConsumption = '';
  let existingSystems = '';
  let financialProfile = '';
  let conversationHistory = '';

  let wantsHeatPump = false;
  let wantsSolarPanels = false;
  let wantsWallbox = false;

  let errors: Record<string, string> = {};
  let isSaving = false;
  let successMessage = '';

  function validate() {
    errors = {};
    if (!firstName.trim()) errors.firstName = 'First name is required';
    if (!lastName.trim()) errors.lastName = 'Last name is required';
    if (!birthDate) errors.birthDate = 'Date of birth is required';
    if (!zip.trim()) errors.zip = 'ZIP code is required';
    if (!city.trim()) errors.city = 'City is required';
    if (!street.trim()) errors.street = 'Street is required';
    if (!houseNumber.trim()) errors.houseNumber = 'House number is required';

    return Object.keys(errors).length === 0;
  }

  async function handleSave() {
    successMessage = '';
    if (validate()) {
      isSaving = true;
      const customerDTO: CreateCustomerDTO = {
        required: {
          firstName,
          lastName,
          birthDate,
          address: {
            zip,
            city,
            street,
            houseNumber
          }
        },
        optional: {
          customerProfile: customerProfile.trim() || undefined,
          energyConsumption: energyConsumption.trim() || undefined,
          existingSystems: existingSystems.trim() || undefined,
          financialProfile: financialProfile.trim() || undefined,
          conversationHistory: conversationHistory.trim() || undefined,
          wantsHeatPump,
          wantsSolarPanels,
          wantsWallbox
        }
      };

      try {
        const response = await customerService.saveCustomer(customerDTO);
        
        if (response.success && response.id) {
          successMessage = 'Customer successfully created';
          
          // Navigiere zur Customer-Page nach erfolgreichem Speichern
          window.history.pushState({}, '', `/customer/${response.id}`);
          window.dispatchEvent(new PopStateEvent('popstate'));
        } else {
          errors.general = 'An error occurred';
        }
      } catch (err) {
        errors.general = 'An error occurred';
      } finally {
        isSaving = false;
      }
    }
  }
</script>

<main class="container">
  <div class="form-card card">
    <div class="form-header">
      <div class="header-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="17" y1="11" x2="23" y2="11"></line></svg>
      </div>
      <div>
        <h1>Add new customer</h1>
        <p class="subtitle">Enter the basic details to start the AI coaching process.</p>
      </div>
    </div>

    <form onsubmit={(e) => { e.preventDefault(); handleSave(); }}>
      <div class="form-sections-wrapper">
        <!-- Required Info Section -->
        <div class="form-section">
          <h2 class="section-title">Personal Information</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="firstName">First name *</label>
              <input 
                type="text" 
                id="firstName" 
                bind:value={firstName} 
                placeholder="e.g. Max"
                aria-invalid={!!errors.firstName}
                class:error={!!errors.firstName}
              />
              {#if errors.firstName}<span class="error-text">{errors.firstName}</span>{/if}
            </div>

            <div class="form-group">
              <label for="lastName">Last name *</label>
              <input 
                type="text" 
                id="lastName" 
                bind:value={lastName} 
                placeholder="e.g. Smith"
                aria-invalid={!!errors.lastName}
                class:error={!!errors.lastName}
              />
              {#if errors.lastName}<span class="error-text">{errors.lastName}</span>{/if}
            </div>

            <div class="form-group full-width">
              <label for="birthDate">Date of birth *</label>
              <input 
                type="date" 
                id="birthDate" 
                bind:value={birthDate} 
                aria-invalid={!!errors.birthDate}
                class:error={!!errors.birthDate}
              />
              {#if errors.birthDate}<span class="error-text">{errors.birthDate}</span>{/if}
            </div>
          </div>
        </div>

        <!-- Address Section -->
        <div class="form-section">
          <h2 class="section-title">Property Address</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label for="street">Street *</label>
              <input 
                type="text" 
                id="street" 
                bind:value={street} 
                placeholder="Main Street"
                aria-invalid={!!errors.street}
                class:error={!!errors.street}
              />
              {#if errors.street}<span class="error-text">{errors.street}</span>{/if}
            </div>

            <div class="form-group">
              <label for="houseNumber">House number *</label>
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
              <label for="zip">ZIP *</label>
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

            <div class="form-group full-width">
              <label for="city">City *</label>
              <input 
                type="text" 
                id="city" 
                bind:value={city} 
                placeholder="London"
                aria-invalid={!!errors.city}
                class:error={!!errors.city}
              />
              {#if errors.city}<span class="error-text">{errors.city}</span>{/if}
            </div>
          </div>
        </div>

        <!-- Additional Info Section -->
        <div class="form-section optional-section">
          <div class="section-header">
            <h2 class="section-title">Product Interests</h2>
            <span class="badge badge-accent">Optional</span>
          </div>
          <div class="checkbox-grid">
            <label class="checkbox-container">
              <input type="checkbox" bind:checked={wantsHeatPump} />
              <span class="checkbox-custom"></span>
              <span class="checkbox-label">Heat Pump</span>
            </label>
            <label class="checkbox-container">
              <input type="checkbox" bind:checked={wantsSolarPanels} />
              <span class="checkbox-custom"></span>
              <span class="checkbox-label">Solar Panels</span>
            </label>
            <label class="checkbox-container">
              <input type="checkbox" bind:checked={wantsWallbox} />
              <span class="checkbox-custom"></span>
              <span class="checkbox-label">Wallbox</span>
            </label>
          </div>
        </div>

        <!-- Additional Info Section -->
        <div class="form-section optional-section">
          <div class="section-header">
            <h2 class="section-title">Additional Details</h2>
            <span class="badge badge-accent">Optional</span>
          </div>
          
          <div class="form-grid">
            <div class="form-group full-width">
              <label for="customerProfile">Customer Profile</label>
              <input 
                type="text" 
                id="customerProfile" 
                bind:value={customerProfile} 
                placeholder="e.g. 4-person household, single-family house built in 1985"
              />
            </div>

            <div class="form-group full-width">
              <label for="energyConsumption">Energy Consumption</label>
              <input 
                type="text" 
                id="energyConsumption" 
                bind:value={energyConsumption} 
                placeholder="e.g. 4,500 kWh/year electricity, gas heating"
              />
            </div>

            <div class="form-group full-width">
              <label for="existingSystems">Existing Systems</label>
              <input 
                type="text" 
                id="existingSystems" 
                bind:value={existingSystems} 
                placeholder="e.g. Already 5 kWp solar system, no battery storage"
              />
            </div>

            <div class="form-group full-width">
              <label for="financialProfile">Financial Profile</label>
              <input 
                type="text" 
                id="financialProfile" 
                bind:value={financialProfile} 
                placeholder="e.g. Medium income, open to financing"
              />
            </div>

            <div class="form-group full-width">
              <label for="conversationHistory">Conversation History</label>
              <textarea 
                id="conversationHistory" 
                bind:value={conversationHistory} 
                placeholder="e.g. Customer mentioned concerns about rising gas prices..."
                rows="4"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-save" disabled={isSaving}>
          {#if isSaving}
            <div class="loader"></div>
            <span>Creating Customer...</span>
          {:else}
            <span>Save & Continue to Analysis</span>
          {/if}
        </button>
      </div>

      {#if errors.general}
        <div class="alert alert-error" role="alert">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          {errors.general}
        </div>
      {/if}

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
    max-width: 800px;
    margin: 0 auto;
  }

  .form-card {
    padding: 3.5rem;
    border-radius: var(--radius-lg);
    border: 1px solid var(--clr-border);
  }

  .form-header {
    margin-bottom: 3.5rem;
    display: flex;
    align-items: center;
    gap: 1.5rem;
  }

  .header-icon {
    width: 60px;
    height: 60px;
    background: var(--clr-accent-light);
    color: var(--clr-accent-dark);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  h1 {
    font-size: 2.25rem;
    margin-bottom: 0.25rem;
    letter-spacing: -0.03em;
  }

  .subtitle {
    color: var(--clr-text-muted);
    font-size: 1.125rem;
  }

  .form-sections-wrapper {
    display: flex;
    flex-direction: column;
    gap: 3.5rem;
  }

  .form-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 0.5rem;
  }

  .section-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--clr-primary);
    margin: 0;
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
    gap: 0.625rem;
  }

  label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-primary-light);
  }

  input, textarea {
    padding: 0.875rem 1.125rem;
    border: 1.5px solid var(--clr-border);
    border-radius: var(--radius-md);
    font-size: 1rem;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    background-color: white;
  }

  input:focus, textarea:focus {
    outline: none;
    border-color: var(--clr-accent);
    box-shadow: 0 0 0 4px var(--clr-accent-light);
  }

  input.error {
    border-color: var(--clr-error);
    background-color: #fef2f2;
  }

  .error-text {
    color: var(--clr-error);
    font-size: 0.8125rem;
    font-weight: 600;
    margin-top: -0.25rem;
  }

  /* Checkbox Styles */
  .checkbox-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1.25rem;
    margin-top: 1.5rem;
  }

  .checkbox-container {
    display: flex;
    align-items: center;
    position: relative;
    padding-left: 2.5rem;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 600;
    color: var(--clr-primary);
    user-select: none;
    transition: all 0.2s;
    height: 3rem;
    background: white;
    border: 1.5px solid var(--clr-border);
    border-radius: var(--radius-md);
    padding-right: 1rem;
  }

  .checkbox-container:hover {
    border-color: var(--clr-accent);
    background: var(--clr-accent-light);
  }

  .checkbox-container input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
    height: 0;
    width: 0;
  }

  .checkbox-custom {
    position: absolute;
    top: 50%;
    left: 1rem;
    transform: translateY(-50%);
    height: 1.25rem;
    width: 1.25rem;
    background-color: white;
    border: 2px solid var(--clr-border);
    border-radius: 4px;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .checkbox-container input:checked ~ .checkbox-custom {
    background-color: var(--clr-accent);
    border-color: var(--clr-accent);
  }

  .checkbox-custom:after {
    content: "";
    position: absolute;
    display: none;
    left: 7px;
    top: 3px;
    width: 5px;
    height: 10px;
    border: solid white;
    border-width: 0 2.5px 2.5px 0;
    transform: rotate(45deg);
  }

  .checkbox-container input:checked ~ .checkbox-custom:after {
    display: block;
  }

  .checkbox-container input:checked ~ .checkbox-label {
    color: var(--clr-primary);
  }

  .form-actions {
    margin-top: 4rem;
    padding-top: 2.5rem;
    border-top: 1px solid var(--clr-border);
  }

  .btn-save {
    background-color: var(--clr-primary);
    color: white;
    border: none;
    padding: 1.25rem;
    border-radius: var(--radius-lg);
    font-size: 1.125rem;
    font-weight: 700;
    width: 100%;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.2);
  }

  .btn-save:hover:not(:disabled) {
    background-color: #1e293b;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.25);
  }

  .btn-save:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .loader {
    width: 20px;
    height: 20px;
    border: 3px solid rgba(255,255,255,0.3);
    border-radius: 50%;
    border-top-color: white;
    animation: spin 0.8s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .alert {
    margin-top: 2rem;
    padding: 1.25rem;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    gap: 1rem;
    font-weight: 600;
  }

  .alert-success {
    background-color: var(--clr-accent-light);
    color: var(--clr-accent-dark);
    border: 1px solid rgba(16, 185, 129, 0.2);
  }

  .alert-error {
    background-color: #fef2f2;
    color: var(--clr-error);
    border: 1px solid #fee2e2;
  }

  @media (max-width: 768px) {
    .form-card {
      padding: 2rem;
    }
    .form-grid {
      grid-template-columns: 1fr;
    }
    .full-width {
      grid-column: span 1;
    }
    h1 {
      font-size: 1.75rem;
    }
  }
</style>
