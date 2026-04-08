import type { Customer, CreateCustomerDTO, PropertyRequestDto, CustomerResponseDto } from './types';

// Dummy-Daten für das MVP
const dummyCustomers: Customer[] = [
  {
    id: '1',
    firstName: 'Max',
    lastName: 'Mustermann',
    birthDate: '1985-05-20',
    address: {
      zip: '10115',
      city: 'Berlin',
      street: 'Friedrichstraße',
      houseNumber: '10'
    }
  },
  {
    id: '2',
    firstName: 'Anna',
    lastName: 'Schmidt',
    birthDate: '1992-08-15',
    address: {
      zip: '20095',
      city: 'Hamburg',
      street: 'Mönckebergstraße',
      houseNumber: '5'
    }
  },
  {
    id: '3',
    firstName: 'John',
    lastName: 'Doe',
    birthDate: '1978-01-10',
    address: {
      zip: 'SW1A 1AA',
      city: 'London',
      street: 'Downing Street',
      houseNumber: '10'
    }
  },
  {
    id: '4',
    firstName: 'Marie',
    lastName: 'Curie',
    birthDate: '1867-11-07',
    address: {
      zip: '75005',
      city: 'Paris',
      street: 'Rue Soufflot',
      houseNumber: '1'
    }
  }
];

export const customerService = {
  /**
   * Holt alle Kunden vom Backend.
   */
  async getAllCustomers(): Promise<Customer[]> {
    try {
      const response = await fetch('http://localhost:8080/customers');

      if (response.ok) {
        const dtos: CustomerResponseDto[] = await response.json();
        return dtos.map(dto => ({
          id: dto.id.toString(),
          firstName: dto.firstName,
          lastName: dto.lastName,
          birthDate: dto.birthDate
        }));
      }
      
      throw new Error(`Failed to fetch customers: ${response.status}`);
    } catch (err) {
      console.warn('Backend call failed, using dummy data:', err);
      // Fallback auf Dummy-Daten für die Entwicklung
      return new Promise((resolve) => {
        setTimeout(() => resolve([...dummyCustomers]), 200);
      });
    }
  },

  /**
   * Holt einen Kunden anhand seiner ID.
   */
  async getCustomerById(id: string): Promise<Customer | null> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const customer = dummyCustomers.find(c => c.id === id) || null;
        resolve(customer);
      }, 100);
    });
  },

  /**
   * Sucht nach Kunden basierend auf einem Suchbegriff (Vorname oder Nachname).
   * Diese Funktion simuliert einen Backend-Call.
   */
  async searchCustomers(query: string): Promise<Customer[]> {
    if (!query.trim()) {
      return [];
    }

    const searchTerm = query.toLowerCase();
    
    // Simuliere Latenz für die Backend-Vorbereitung
    return new Promise((resolve) => {
      setTimeout(() => {
        const results = dummyCustomers.filter(customer => 
          customer.firstName.toLowerCase().includes(searchTerm) || 
          customer.lastName.toLowerCase().includes(searchTerm) ||
          (customer.address?.city.toLowerCase().includes(searchTerm))
        );
        // Wir stellen sicher, dass die Objekte in den Ergebnissen Kopien sind und die IDs korrekt sind
        resolve(results.map(r => ({ ...r })));
      }, 300); // 300ms künstliche Verzögerung
    });
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
