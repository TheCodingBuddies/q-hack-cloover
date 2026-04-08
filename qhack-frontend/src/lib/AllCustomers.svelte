<script lang="ts">
  import { onMount } from 'svelte';
  import { customerService } from './customerService';
  import type { Customer } from './types';

  interface Project {
    id: string;
    name: string;
    progress: number;
    location: string;
    offerPreview: string;
    aiHints: string[];
  }

  const mockProjects: Record<string, Project[]> = {
    'Max Mustermann': [
      {
        id: 'p1',
        name: 'Solar Installation Berlin',
        progress: 65,
        location: 'Friedrichstraße 10, 10115 Berlin',
        offerPreview: `**Offer #2024-001**\n\nSolar panel installation for residential property.\n\n- 12x 400W panels\n- 1x 10kWh battery storage\n- Grid feed-in setup\n\n**Total: €18,400**\n\nValidity: 30 days`,
        aiHints: [
          'Ask about current electricity bill to calculate ROI',
          'Roof orientation is south-west — ideal for yield',
          'Customer mentioned interest in EV charging in future',
        ],
      },
      {
        id: 'p2',
        name: 'Heat Pump Replacement',
        progress: 20,
        location: 'Friedrichstraße 10, 10115 Berlin',
        offerPreview: `**Offer #2024-002**\n\nHeat pump replacement project.\n\nDetails pending customer site visit.`,
        aiHints: [
          'Building year: 1985 — check insulation status',
          'Ask for current heating costs',
        ],
      },
    ],
    'Anna Schmidt': [
      {
        id: 'p3',
        name: 'Photovoltaik Hamburg',
        progress: 90,
        location: 'Mönckebergstraße 5, 20095 Hamburg',
        offerPreview: `**Offer #2024-003**\n\nPV system for Hamburg property.\n\n- 8x 380W panels\n- Monitoring system included\n\n**Total: €11,200**\n\nReady for signature.`,
        aiHints: [
          'Offer nearly complete — follow up on signature',
          'Customer is price-sensitive, highlight long-term savings',
        ],
      },
    ],
    'John Doe': [
      {
        id: 'p4',
        name: 'Commercial Solar London',
        progress: 40,
        location: 'Downing Street 10, SW1A 1AA London',
        offerPreview: `**Offer #2024-004**\n\nCommercial rooftop installation.\n\nAwaiting structural survey results.`,
        aiHints: [
          'Check UK grid connection regulations',
          'Ask about planning permission status',
          'Potential for larger system — explore 50kW+ option',
        ],
      },
    ],
    'Marie Curie': [],
  };

  let customers: Customer[] = [];
  let isLoading = true;
  let sidebarSearch = '';
  let expandedCustomer: string | null = null;
  let selectedProject: Project | null = null;
  let selectedCustomer: Customer | null = null;

  let filteredCustomers: Customer[] = [];
  $: filteredCustomers = customers.filter(c =>
    `${c.firstName} ${c.lastName}`.toLowerCase().includes(sidebarSearch.toLowerCase())
  );

  onMount(async () => {
    customers = await customerService.getAllCustomers();
    isLoading = false;
  });

  function customerKey(c: Customer) {
    return `${c.firstName} ${c.lastName}`;
  }

  function toggleCustomer(c: Customer) {
    const key = customerKey(c);
    if (expandedCustomer === key) {
      expandedCustomer = null;
      selectedProject = null;
      selectedCustomer = null;
    } else {
      expandedCustomer = key;
      selectedCustomer = c;
      const projects = mockProjects[key] ?? [];
      selectedProject = projects[0] ?? null;
    }
  }

  function selectProject(c: Customer, p: Project) {
    selectedCustomer = c;
    selectedProject = p;
  }

  let addingProjectFor: string | null = null;
  let addingProjectCustomer: Customer | null = null;
  let newProjectName = '';

  function startAddProject(key: string) {
    addingProjectFor = key;
    addingProjectCustomer = customers.find(c => customerKey(c) === key) ?? null;
    newProjectName = '';
  }

  function cancelAddProject() {
    addingProjectFor = null;
    addingProjectCustomer = null;
    newProjectName = '';
  }

  function onModalKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape') cancelAddProject();
  }

  function onModalSubmit(e: SubmitEvent) {
    e.preventDefault();
    if (addingProjectCustomer) confirmAddProject(addingProjectCustomer);
  }

  function confirmAddProject(customer: Customer) {
    const key = customerKey(customer);
    if (!newProjectName.trim()) return;
    const project: Project = {
      id: `p-${Date.now()}`,
      name: newProjectName.trim(),
      progress: 0,
      location: `${customer.address.street} ${customer.address.houseNumber}, ${customer.address.zip} ${customer.address.city}`,
      offerPreview: 'No offer created yet.',
      aiHints: ['Start by filling in the project details'],
    };
    mockProjects[key] = [...(mockProjects[key] ?? []), project];
    addingProjectFor = null;
    newProjectName = '';
    selectProject(customer, project);
  }

  // Edit customer
  let editingCustomer = false;
  let editCustomerForm = { firstName: '', lastName: '', birthDate: '', street: '', houseNumber: '', zip: '', city: '' };

  function openEditCustomer() {
    if (!selectedCustomer) return;
    editCustomerForm = {
      firstName: selectedCustomer.firstName,
      lastName: selectedCustomer.lastName,
      birthDate: selectedCustomer.birthDate,
      street: selectedCustomer.address.street,
      houseNumber: selectedCustomer.address.houseNumber,
      zip: selectedCustomer.address.zip,
      city: selectedCustomer.address.city,
    };
    editingCustomer = true;
  }

  function saveEditCustomer() {
    if (!selectedCustomer) return;
    const oldKey = customerKey(selectedCustomer);
    selectedCustomer.firstName = editCustomerForm.firstName;
    selectedCustomer.lastName = editCustomerForm.lastName;
    selectedCustomer.birthDate = editCustomerForm.birthDate;
    selectedCustomer.address = {
      street: editCustomerForm.street,
      houseNumber: editCustomerForm.houseNumber,
      zip: editCustomerForm.zip,
      city: editCustomerForm.city,
    };
    const newKey = customerKey(selectedCustomer);
    if (oldKey !== newKey && mockProjects[oldKey]) {
      mockProjects[newKey] = mockProjects[oldKey];
      delete mockProjects[oldKey];
    }
    customers = [...customers];
    expandedCustomer = newKey;
    editingCustomer = false;
  }

  function onEditCustomerSubmit(e: SubmitEvent) {
    e.preventDefault();
    saveEditCustomer();
  }

  // Edit project
  let editingProject = false;
  let editProjectForm = { name: '', location: '' };

  function openEditProject() {
    if (!selectedProject) return;
    editProjectForm = { name: selectedProject.name, location: selectedProject.location };
    editingProject = true;
  }

  function saveEditProject() {
    if (!selectedProject || !selectedCustomer) return;
    selectedProject.name = editProjectForm.name;
    selectedProject.location = editProjectForm.location;
    const key = customerKey(selectedCustomer);
    mockProjects[key] = [...(mockProjects[key] ?? [])];
    selectedProject = { ...selectedProject };
    editingProject = false;
  }

  function onEditProjectSubmit(e: SubmitEvent) {
    e.preventDefault();
    saveEditProject();
  }

  function onEditModalKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape') { editingCustomer = false; editingProject = false; }
  }

  function progressLabel(p: number) {
    if (p < 25) return 'Just started';
    if (p < 50) return 'In progress';
    if (p < 75) return 'Advanced';
    if (p < 100) return 'Nearly done';
    return 'Complete';
  }
</script>

<div class="layout">
  <!-- Sidebar -->
  <aside class="sidebar">
    <div class="sidebar-header">Customers</div>
    <div class="sidebar-search">
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input type="text" placeholder="Search..." bind:value={sidebarSearch} />
    </div>

    {#if isLoading}
      <div class="sidebar-loading">Loading...</div>
    {:else}
      {#each filteredCustomers as customer}
        {@const key = customerKey(customer)}
        {@const projects = mockProjects[key] ?? []}
        <div class="customer-entry">
          <button
            class="customer-btn"
            class:active={expandedCustomer === key}
            onclick={() => toggleCustomer(customer)}
          >
            <span class="customer-avatar">{customer.firstName[0]}{customer.lastName[0]}</span>
            <span class="customer-label">{customer.firstName} {customer.lastName}</span>
            <svg class="chevron" class:rotated={expandedCustomer === key} xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>

          {#if expandedCustomer === key}
            <div class="project-list">
              <button class="btn-add-project-sidebar" onclick={() => startAddProject(key)}>
                <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Add project
              </button>
              {#each projects as project}
                <button
                  class="project-btn"
                  class:active={selectedProject?.id === project.id}
                  onclick={() => selectProject(customer, project)}
                >
                  <span class="project-btn-name">{project.name}</span>
                  <div class="project-btn-progress">
                    <div class="project-btn-track">
                      <div class="project-btn-fill" style="width: {project.progress}%"></div>
                    </div>
                    <span class="project-btn-pct">{project.progress}%</span>
                  </div>
                </button>
              {/each}
            </div>
          {/if}
        </div>
      {/each}
    {/if}
  </aside>

  <!-- Main content -->
  <main class="main">
    {#if selectedProject && selectedCustomer}
      <div class="main-header">
        <div class="main-header-left">
          <span class="project-tag">Project</span>
          <div class="main-header-title-row">
            <h2>{selectedProject.name}</h2>
            <button class="btn-present">Present project</button>
          </div>
          <div class="progress-bar-wrapper">
            <div class="progress-track">
              <div class="progress-fill" style="width: {selectedProject.progress}%"></div>
            </div>
            <span class="progress-label">{selectedProject.progress}% — {progressLabel(selectedProject.progress)}</span>
          </div>
        </div>
      </div>

      <div class="main-body">
        <!-- Offer preview -->
        <div class="offer-preview card">
          <div class="panel-title">Offer preview</div>
          <div class="offer-text">
            {#each selectedProject.offerPreview.split('\n') as line}
              {#if line.startsWith('**') && line.endsWith('**')}
                <strong>{line.slice(2, -2)}</strong><br/>
              {:else if line.startsWith('- ')}
                <li>{line.slice(2)}</li>
              {:else if line.trim() === ''}
                <br/>
              {:else}
                <span>{line}</span><br/>
              {/if}
            {/each}
          </div>
        </div>

        <!-- Right panels -->
        <div class="right-panels">
          <div class="info-card card">
            <div class="card-title-row">
              <div class="panel-title">Customer info</div>
              <button class="btn-edit" aria-label="Edit customer info" onclick={openEditCustomer}>
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </div>
            <div class="info-row">
              <span class="info-label">Name</span>
              <span>{selectedCustomer.firstName} {selectedCustomer.lastName}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Date of birth</span>
              <span>{new Date(selectedCustomer.birthDate).toLocaleDateString()}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Address</span>
              <span>{selectedCustomer.address.street} {selectedCustomer.address.houseNumber}, {selectedCustomer.address.zip} {selectedCustomer.address.city}</span>
            </div>
          </div>

          <div class="info-card card">
            <div class="card-title-row">
              <div class="panel-title">Project info</div>
              <button class="btn-edit" aria-label="Edit project info" onclick={openEditProject}>
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </div>
            <div class="info-row">
              <span class="info-label">Project name</span>
              <span>{selectedProject.name}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Location</span>
              <span>{selectedProject.location}</span>
            </div>
          </div>

          <div class="info-card card">
            <div class="panel-title ai-title">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
              AI hints
            </div>
            <ul class="ai-list">
              {#each selectedProject.aiHints as hint}
                <li>{hint}</li>
              {/each}
            </ul>
          </div>
        </div>
      </div>
    {:else if selectedCustomer && expandedCustomer}
      <div class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 0 0-4 0v2"/></svg>
        <p>No projects yet for {selectedCustomer.firstName} {selectedCustomer.lastName}</p>
        <button class="btn-primary-lg" onclick={() => startAddProject(expandedCustomer ?? '')}>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Add project
        </button>
      </div>
    {:else}
      <div class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        <p>Select a customer and project to view details</p>
      </div>
    {/if}
  </main>
</div>

{#if addingProjectFor !== null && addingProjectCustomer !== null}
  <div class="modal-backdrop" role="presentation" onkeydown={onModalKeydown}>
    <div class="modal-card card" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal-header">
        <h2 id="modal-title">Add new project</h2>
        <p class="modal-subtitle">Enter a name for the new project for {addingProjectCustomer.firstName} {addingProjectCustomer.lastName}.</p>
      </div>
      <form onsubmit={onModalSubmit}>
        <div class="modal-form-group">
          <label for="projectName">Project name *</label>
          <input
            type="text"
            id="projectName"
            bind:value={newProjectName}
            placeholder="e.g. Solar Installation Berlin"
          />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" onclick={cancelAddProject}>Cancel</button>
          <button type="submit" class="btn-modal-save" disabled={!newProjectName.trim()}>
            Save project
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

{#if editingCustomer && selectedCustomer}
  <div class="modal-backdrop" role="presentation" onkeydown={onEditModalKeydown}>
    <div class="modal-card card" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="edit-customer-title">
      <div class="modal-header">
        <h2 id="edit-customer-title">Edit customer info</h2>
        <p class="modal-subtitle">Update the details for {selectedCustomer.firstName} {selectedCustomer.lastName}.</p>
      </div>
      <form onsubmit={onEditCustomerSubmit}>
        <div class="modal-grid">
          <div class="modal-form-group">
            <label for="ec-firstName">First name *</label>
            <input id="ec-firstName" type="text" bind:value={editCustomerForm.firstName} placeholder="First name" />
          </div>
          <div class="modal-form-group">
            <label for="ec-lastName">Last name *</label>
            <input id="ec-lastName" type="text" bind:value={editCustomerForm.lastName} placeholder="Last name" />
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-birthDate">Date of birth *</label>
            <input id="ec-birthDate" type="date" bind:value={editCustomerForm.birthDate} />
          </div>
          <div class="modal-form-group modal-full modal-section-title">Address</div>
          <div class="modal-form-group modal-full">
            <label for="ec-street">Street *</label>
            <input id="ec-street" type="text" bind:value={editCustomerForm.street} placeholder="Street" />
          </div>
          <div class="modal-form-group">
            <label for="ec-houseNumber">House number *</label>
            <input id="ec-houseNumber" type="text" bind:value={editCustomerForm.houseNumber} placeholder="10a" />
          </div>
          <div class="modal-form-group">
            <label for="ec-zip">ZIP *</label>
            <input id="ec-zip" type="text" bind:value={editCustomerForm.zip} placeholder="12345" />
          </div>
          <div class="modal-form-group modal-full">
            <label for="ec-city">City *</label>
            <input id="ec-city" type="text" bind:value={editCustomerForm.city} placeholder="City" />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" onclick={() => editingCustomer = false}>Cancel</button>
          <button type="submit" class="btn-modal-save">Save changes</button>
        </div>
      </form>
    </div>
  </div>
{/if}

{#if editingProject && selectedProject}
  <div class="modal-backdrop" role="presentation" onkeydown={onEditModalKeydown}>
    <div class="modal-card card" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="edit-project-title">
      <div class="modal-header">
        <h2 id="edit-project-title">Edit project info</h2>
        <p class="modal-subtitle">Update the details for this project.</p>
      </div>
      <form onsubmit={onEditProjectSubmit}>
        <div class="modal-form-group">
          <label for="ep-name">Project name *</label>
          <input id="ep-name" type="text" bind:value={editProjectForm.name} placeholder="Project name" />
        </div>
        <div class="modal-form-group">
          <label for="ep-location">Location *</label>
          <input id="ep-location" type="text" bind:value={editProjectForm.location} placeholder="Street, ZIP City" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" onclick={() => editingProject = false}>Cancel</button>
          <button type="submit" class="btn-modal-save">Save changes</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<style>
  .layout {
    display: flex;
    height: calc(100vh - 70px);
    overflow: hidden;
  }

  /* Sidebar */
  .sidebar {
    width: 260px;
    flex-shrink: 0;
    background: white;
    border-right: 1px solid var(--clr-border);
    overflow-y: auto;
    display: flex;
    flex-direction: column;
  }

  .sidebar-header {
    padding: 1.25rem 1.25rem 0.75rem;
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--clr-text-light);
    border-bottom: 1px solid var(--clr-border);
    margin-bottom: 0.5rem;
  }

  .sidebar-search {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 1.25rem;
    border-bottom: 1px solid var(--clr-border);
    color: var(--clr-text-light);
  }

  .sidebar-search input {
    border: none;
    background: transparent;
    outline: none;
    font-size: 0.875rem;
    width: 100%;
    color: var(--clr-text);
    font-family: inherit;
  }

  .sidebar-search input::placeholder {
    color: var(--clr-text-light);
  }

  .sidebar-loading {
    padding: 1rem 1.25rem;
    color: var(--clr-text-light);
    font-size: 0.875rem;
  }

  .customer-entry {
    border-bottom: 1px solid var(--clr-border);
  }

  .customer-btn {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 0.625rem;
    padding: 0.75rem 1.25rem;
    background: none;
    border: none;
    text-align: left;
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--clr-text);
    transition: background 0.15s;
  }

  .customer-btn:hover,
  .customer-btn.active {
    background: var(--clr-bg-alt);
    color: var(--clr-primary);
  }

  .customer-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: var(--clr-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.7rem;
    font-weight: 700;
    flex-shrink: 0;
  }

  .customer-label {
    flex: 1;
  }

  .chevron {
    color: var(--clr-text-light);
    transition: transform 0.2s;
  }

  .chevron.rotated {
    transform: rotate(180deg);
  }

  .project-list {
    padding: 0.25rem 0 0.75rem;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }


  .project-btn {
    display: flex;
    flex-direction: column;
    width: calc(100% - 2rem);
    margin: 0 1rem;
    padding: 0.6rem 0.75rem;
    background: white;
    border: 1px solid var(--clr-border);
    border-radius: 8px;
    text-align: left;
    color: var(--clr-text);
    transition: border-color 0.15s, box-shadow 0.15s;
  }

  .project-btn:hover {
    border-color: var(--clr-accent);
    box-shadow: var(--shadow-sm);
  }

  .project-btn.active {
    border-color: var(--clr-accent);
    background: #ecfdf5;
    box-shadow: var(--shadow-sm);
  }

  .project-btn-name {
    font-size: 0.825rem;
    font-weight: 600;
    text-align: left;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .project-btn-progress {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 0.35rem;
  }

  .project-btn-track {
    flex: 1;
    height: 4px;
    background: var(--clr-border);
    border-radius: 99px;
    overflow: hidden;
  }

  .project-btn-fill {
    height: 100%;
    background: var(--clr-accent);
    border-radius: 99px;
  }

  .project-btn-pct {
    font-size: 0.7rem;
    color: var(--clr-text-light);
    white-space: nowrap;
  }

  .btn-add-project-sidebar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    width: calc(100% - 2rem);
    margin: 0.5rem 1rem;
    padding: 0.6rem 1rem;
    background: var(--clr-primary);
    color: white;
    border: none;
    border-radius: var(--radius-md);
    font-size: 0.825rem;
    font-weight: 600;
    transition: background 0.2s, transform 0.2s;
  }

  .btn-add-project-sidebar:hover {
    background: #1e293b;
    transform: translateY(-1px);
  }

  /* Modal */
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: flex-start;
    justify-content: center;
    z-index: 200;
    padding: 2rem 1rem;
    overflow-y: auto;
  }

  .modal-card {
    width: 100%;
    max-width: 520px;
    padding: 2rem;
  }

  .modal-header {
    margin-bottom: 1.5rem;
  }

  .modal-header h2 {
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
  }

  .modal-subtitle {
    color: var(--clr-text-light);
    font-size: 0.95rem;
    margin: 0;
  }

  .modal-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .modal-full {
    grid-column: span 2;
  }

  .modal-section-title {
    font-size: 0.875rem;
    font-weight: 700;
    color: var(--clr-primary);
    padding-top: 0.5rem;
    border-top: 1px solid var(--clr-border);
    margin-bottom: -0.25rem;
    align-items: center;
    display: flex;
  }

  .modal-form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 0;
  }

  .modal-form-group label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--clr-primary);
  }

  .modal-form-group input {
    padding: 0.75rem 1rem;
    border: 1px solid var(--clr-border);
    border-radius: 8px;
    font-size: 1rem;
    font-family: inherit;
    background: var(--clr-bg-alt);
    transition: all 0.2s;
  }

  .modal-form-group input:focus {
    outline: none;
    border-color: var(--clr-accent);
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
    background: white;
  }

  .modal-actions {
    display: flex;
    gap: 0.75rem;
  }

  .btn-modal-cancel {
    flex: 1;
    padding: 0.875rem;
    background: none;
    border: 1px solid var(--clr-border);
    border-radius: var(--radius-md);
    font-size: 0.95rem;
    font-weight: 500;
    color: var(--clr-text-light);
    transition: border-color 0.2s, color 0.2s;
  }

  .btn-modal-cancel:hover {
    border-color: var(--clr-primary);
    color: var(--clr-primary);
  }

  .btn-modal-save {
    flex: 2;
    padding: 0.875rem;
    background: var(--clr-primary);
    color: white;
    border: none;
    border-radius: var(--radius-md);
    font-size: 0.95rem;
    font-weight: 600;
    transition: background 0.2s, transform 0.2s;
  }

  .btn-modal-save:hover:not(:disabled) {
    background: #1e293b;
    transform: translateY(-1px);
  }

  .btn-modal-save:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  /* Main */
  .main {
    flex: 1;
    overflow-y: auto;
    padding: 1.75rem 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .main-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
  }

  .main-header-left {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    flex: 1;
  }

  .project-tag {
    display: inline-block;
    background: var(--clr-bg-alt);
    border: 1px solid var(--clr-border);
    border-radius: 99px;
    padding: 0.2rem 0.75rem;
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--clr-text-light);
    width: fit-content;
  }

  .main-header-left h2 {
    margin: 0;
    font-size: 1.5rem;
  }

  .progress-bar-wrapper {
    display: flex;
    align-items: center;
    gap: 1rem;
    max-width: 600px;
  }

  .progress-track {
    flex: 1;
    height: 8px;
    background: var(--clr-border);
    border-radius: 99px;
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: var(--clr-accent);
    border-radius: 99px;
    transition: width 0.4s ease;
  }

  .progress-label {
    font-size: 0.8rem;
    color: var(--clr-text-light);
    white-space: nowrap;
  }


  .main-body {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 1.5rem;
    flex: 1;
  }

  .offer-preview {
    min-height: 320px;
  }

  .right-panels {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .main-header-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .main-header-title-row h2 {
    margin: 0;
  }

  .btn-present {
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 0.625rem 1.25rem;
    border-radius: var(--radius-md);
    font-size: 0.875rem;
    font-weight: 600;
    white-space: nowrap;
    flex-shrink: 0;
    transition: background 0.2s, transform 0.2s;
  }

  .btn-present:hover {
    background: #1e293b;
    transform: translateY(-1px);
  }

  .panel-title {
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--clr-text-light);
    margin-bottom: 1rem;
  }


  .offer-text {
    font-size: 0.9rem;
    line-height: 1.7;
    color: var(--clr-text);
  }

  .offer-text li {
    margin-left: 1rem;
  }

  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1rem;
  }

  .card-title-row .panel-title {
    margin-bottom: 0;
  }

  .btn-edit {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    background: none;
    border: 1px solid var(--clr-border);
    border-radius: 6px;
    color: var(--clr-text-light);
    flex-shrink: 0;
    transition: border-color 0.15s, color 0.15s, background 0.15s;
  }

  .btn-edit:hover {
    border-color: var(--clr-primary);
    color: var(--clr-primary);
    background: var(--clr-bg-alt);
  }

  .info-card {
    padding: 1.25rem 1.5rem;
  }

  .info-row {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
    margin-bottom: 0.75rem;
    font-size: 0.875rem;
  }

  .info-row:last-child {
    margin-bottom: 0;
  }

  .info-label {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--clr-text-light);
  }


  .ai-title {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    color: var(--clr-accent);
  }

  .ai-list {
    margin: 0;
    padding-left: 1.25rem;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: var(--clr-text);
  }

  .ai-list li {
    line-height: 1.5;
  }

  .btn-primary-lg {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: var(--clr-primary);
    color: white;
    border: none;
    padding: 0.875rem 2rem;
    border-radius: var(--radius-md);
    font-size: 1rem;
    font-weight: 600;
    transition: all 0.2s;
  }

  .btn-primary-lg:hover {
    background: #1e293b;
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
  }

  /* Empty state */
  .empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    color: var(--clr-text-light);
  }

  .empty-state p {
    font-size: 1rem;
    margin: 0;
  }

  @media (max-width: 900px) {
    .main-body {
      grid-template-columns: 1fr;
    }
    .sidebar {
      width: 220px;
    }
  }
</style>
