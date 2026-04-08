import type {
  Customer,
  CreateCustomerDTO,
  PropertyRequestDto,
  CustomerWithPropertyResponseDto,
} from "./types";

function mapDtoToCustomer(dto: CustomerWithPropertyResponseDto): Customer {
  return {
    id: dto.id.toString(),
    firstName: dto.firstName,
    lastName: dto.lastName,
    birthDate: dto.birthDate ?? "",
    address: dto.property
      ? {
          zip: dto.property.postCode,
          city: dto.property.city,
          street: dto.property.street,
          houseNumber: dto.property.houseNumber,
        }
      : undefined,
  };
}

export const customerService = {
  /**
   * Holt alle Kunden vom Backend.
   */
  async getAllCustomers(): Promise<Customer[]> {
    const response = await fetch("http://localhost:8080/customers");

    if (!response.ok) {
      throw new Error(`Failed to fetch customers: ${response.status}`);
    }

    const dtos: CustomerWithPropertyResponseDto[] = await response.json();
    return dtos.map(mapDtoToCustomer);
  },

  /**
   * Holt einen einzelnen Kunden anhand seiner ID vom Backend.
   * Gibt null zurück bei 404, wirft einen Fehler bei anderen Fehlern.
   */
  async getCustomerById(id: string): Promise<Customer | null> {
    const response = await fetch(`http://localhost:8080/customer/${id}`);

    if (response.status === 404) {
      return null;
    }

    if (!response.ok) {
      throw new Error(`Failed to fetch customer ${id}: ${response.status}`);
    }

    const dto: CustomerWithPropertyResponseDto = await response.json();
    return mapDtoToCustomer(dto);
  },

  /**
   * Sucht nach Kunden basierend auf einem Suchbegriff (Vorname, Nachname oder Stadt).
   * Holt alle Kunden vom Backend und filtert clientseitig.
   */
  async searchCustomers(query: string): Promise<Customer[]> {
    if (!query.trim()) {
      return [];
    }

    const searchTerm = query.toLowerCase();
    const allCustomers = await customerService.getAllCustomers();

    return allCustomers.filter(
      (customer) =>
        customer.firstName.toLowerCase().includes(searchTerm) ||
        customer.lastName.toLowerCase().includes(searchTerm) ||
        customer.address?.city.toLowerCase().includes(searchTerm),
    );
  },

  /**
   * Speichert einen neuen Kunden gegen den Backend-Endpunkt.
   */
  async saveCustomer(
    dto: CreateCustomerDTO,
  ): Promise<{ success: boolean; id?: string; error?: string }> {
    try {
      const customerRequestData = {
        firstName: dto.required.firstName,
        lastName: dto.required.lastName,
        birthDate: dto.required.birthDate,
      };

      const customerResponse = await fetch(
        "http://localhost:8080/add-customer",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(customerRequestData),
        },
      );

      if (!customerResponse.ok) {
        throw new Error(
          `Failed to create customer: Server responded with ${customerResponse.status}`,
        );
      }

      const responseText = await customerResponse.text();
      const idMatch = responseText.match(/id: (.*)$/);
      const customerId = idMatch ? idMatch[1] : undefined;

      if (!customerId) {
        throw new Error("Customer created, but no ID was returned from server");
      }

      const propertyRequestData: PropertyRequestDto = {
        postCode: dto.required.address.zip,
        street: dto.required.address.street,
        city: dto.required.address.city,
        houseNumber: dto.required.address.houseNumber,
      };

      const propertyResponse = await fetch(
        `http://localhost:8080/customer/${customerId}/add-property`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(propertyRequestData),
        },
      );

      if (!propertyResponse.ok) {
        console.warn(
          "Customer created, but failed to add property:",
          propertyResponse.status,
        );
        return {
          success: true,
          id: customerId,
          error: "Customer created, but address could not be saved.",
        };
      }

      return { success: true, id: customerId };
    } catch (err) {
      console.error("Error saving customer and property:", err);
      return {
        success: false,
        error: err instanceof Error ? err.message : "Unknown error occurred",
      };
    }
  },
};
