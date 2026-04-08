import type { Customer } from './types';

// Dummy-Daten für das MVP
const dummyCustomers: Customer[] = [
  {
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
          customer.address.city.toLowerCase().includes(searchTerm)
        );
        resolve(results);
      }, 300); // 300ms künstliche Verzögerung
    });
  },

  /**
   * Speichert einen neuen Kunden.
   * Diese Funktion simuliert einen Backend-POST-Request.
   */
  async saveCustomer(customer: Customer): Promise<{ success: boolean; data?: Customer; error?: string }> {
    // Simuliere Latenz für die Backend-Vorbereitung
    return new Promise((resolve) => {
      setTimeout(() => {
        // Im MVP fügen wir den Kunden einfach den lokalen Dummy-Daten hinzu
        // (Hinweis: Dies hält nur für die aktuelle Session im Speicher)
        dummyCustomers.push({ ...customer });
        
        console.log('Backend-Mock: Customer saved successfully', customer);
        
        resolve({
          success: true,
          data: customer
        });
      }, 500); // 500ms künstliche Verzögerung für das Speichern
    });
  }
};
