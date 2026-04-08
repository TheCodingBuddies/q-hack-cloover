import type { Customer, CreateCustomerDTO, PropertyRequestDto, CustomerResponseDto, CustomerWithPropertiesResponseDto } from './types';

export const customerService = {
  /**
   * Mappt ein CustomerResponseDto oder CustomerWithPropertiesResponseDto auf das interne Customer-Modell.
   * Nutzt die erste Property für die Adresse, falls vorhanden.
   */
  mapDtoToCustomer(dto: CustomerResponseDto | CustomerWithPropertiesResponseDto): Customer {
    let address = undefined;
    if (dto.properties && dto.properties.length > 0) {
      const prop = dto.properties[0];
      address = {
        street: prop.street,
        houseNumber: prop.houseNumber,
        city: prop.city,
        zip: prop.postCode
      };
    }

    return {
      id: dto.id.toString(),
      firstName: dto.firstName,
      lastName: dto.lastName,
      birthDate: dto.birthDate,
      address
    };
  },

  /**
   * Holt alle Kunden vom Backend.
   */
  async getAllCustomers(): Promise<Customer[]> {
    const response = await fetch('http://localhost:8080/customers-with-properties');

    if (response.ok) {
      const dtos: CustomerWithPropertiesResponseDto[] = await response.json();
      return dtos.map(dto => this.mapDtoToCustomer(dto));
    }
    
    throw new Error(`Failed to fetch customers: ${response.status}`);
  },

  /**
   * Holt einen Kunden anhand seiner ID vom Backend.
   */
  async getCustomerById(id: string): Promise<Customer | null> {
    const response = await fetch(`http://localhost:8080/customers/${id}/with-properties`);

    if (response.ok) {
      const dto: CustomerWithPropertiesResponseDto = await response.json();
      return this.mapDtoToCustomer(dto);
    }
    
    if (response.status === 404) {
      return null;
    }

    throw new Error(`Failed to fetch customer: ${response.status}`);
  },

  /**
   * Sucht nach Kunden basierend auf einem Suchbegriff (Vorname oder Nachname).
   */
  async searchCustomers(query: string): Promise<Customer[]> {
    if (!query.trim()) {
      return [];
    }

    // In a real application, this would be a backend call.
    // Since we are removing mocks, we should ideally call a search endpoint.
    // If no search endpoint exists yet, we might have to fetch all and filter, 
    // or return an empty array if we strictly want only real backend data.
    // However, the task is "entferne alle user mocks". 
    // Let's assume for now we should still be able to search if possible, 
    // but without dummyCustomers.
    
    try {
      const customers = await this.getAllCustomers();
      const searchTerm = query.toLowerCase();
      return customers.filter(customer => 
        customer.firstName.toLowerCase().includes(searchTerm) || 
        customer.lastName.toLowerCase().includes(searchTerm) ||
        (customer.address?.city.toLowerCase().includes(searchTerm))
      );
    } catch (err) {
      console.error('Search failed:', err);
      return [];
    }
  },

  /**
   * Speichert einen neuen Kunden gegen den Backend-Endpunkt.
   */
  async saveCustomer(dto: CreateCustomerDTO): Promise<{ success: boolean; id?: string; error?: string }> {
    try {
      // 1. Erstelle den Kundenstamm (First Name, Last Name, Birth Date)
      const customerRequestData = {
        firstName: dto.required.firstName,
        lastName: dto.required.lastName,
        birthDate: dto.required.birthDate
      };

      const customerResponse = await fetch('http://localhost:8080/add-customer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(customerRequestData)
      });

      if (!customerResponse.ok) {
        throw new Error(`Failed to create customer: Server responded with ${customerResponse.status}`);
      }

      const responseText = await customerResponse.text();
      // Extrahiere die ID aus "Customer added with id: <id>"
      const idMatch = responseText.match(/id: (.*)$/);
      const customerId = idMatch ? idMatch[1] : undefined;

      if (!customerId) {
        throw new Error('Customer created, but no ID was returned from server');
      }

      // 2. Füge die Adresse (Property) hinzu
      const propertyRequestData: PropertyRequestDto = {
        postCode: dto.required.address.zip,
        street: dto.required.address.street,
        city: dto.required.address.city,
        houseNumber: dto.required.address.houseNumber
      };

      const propertyResponse = await fetch(`http://localhost:8080/customer/${customerId}/add-property`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(propertyRequestData)
      });

      if (!propertyResponse.ok) {
        // Der Kunde wurde angelegt, aber die Adresse schlug fehl
        console.warn('Customer created, but failed to add property:', propertyResponse.status);
        return {
          success: true,
          id: customerId,
          error: 'Customer created, but address could not be saved.'
        };
      }

      return {
        success: true,
        id: customerId
      };
    } catch (err) {
      console.error('Error saving customer and property:', err);
      return {
        success: false,
        error: err instanceof Error ? err.message : 'Unknown error occurred'
      };
    }
  }
};
