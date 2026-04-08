import type { Customer, CreateCustomerDTO } from './types';

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
  async getAllCustomers(): Promise<Customer[]> {
    return new Promise((resolve) => {
      setTimeout(() => resolve([...dummyCustomers]), 200);
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
          customer.address.city.toLowerCase().includes(searchTerm)
        );
        resolve(results);
      }, 300); // 300ms künstliche Verzögerung
    });
  },

  /**
   * Speichert einen neuen Kunden gegen den Backend-Endpunkt.
   */
  async saveCustomer(dto: CreateCustomerDTO): Promise<{ success: boolean; id?: string; error?: string }> {
    try {
      // Wir senden das DTO flach an den Endpunkt, wie vom Backend DTO erwartet
      const requestData = {
        firstName: dto.required.firstName,
        lastName: dto.required.lastName,
        birthDate: dto.required.birthDate,
        // Da das Backend-Snippet nur diese 3 Felder nutzt, aber wir im Frontend mehr haben, 
        // senden wir der Vollständigkeit halber auch die anderen Daten mit (als flaches Objekt).
        // Das Kotlin Snippet zeigt nur die Extraktion der 3 Felder, ignoriert aber evtl. andere.
        // ...dto.required.address,
        // ...dto.optional
      };

      const response = await fetch('http://localhost:8080/add-customer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      });

      if (!response.ok) {
        throw new Error(`Server responded with ${response.status}`);
      }

      const responseText = await response.text();
      // Extrahiere die ID aus "Customer added with id: <id>"
      const idMatch = responseText.match(/id: (.*)$/);
      const id = idMatch ? idMatch[1] : undefined;

      return {
        success: true,
        id
      };
    } catch (err) {
      console.error('Error saving customer:', err);
      return {
        success: false,
        error: err instanceof Error ? err.message : 'Unknown error occurred'
      };
    }
  }
};
