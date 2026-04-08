export interface Address {
  zip: string;
  city: string;
  street: string;
  houseNumber: string;
}

export interface CustomerDetails {
  customerProfile?: string;
  energyConsumption?: string;
  existingSystems?: string;
  financialProfile?: string;
  conversationHistory?: string;
}

export interface Customer {
  firstName: string;
  lastName: string;
  birthDate: string;
  address: Address;
  details?: CustomerDetails;
}

/**
 * Data Transfer Object for creating a new customer.
 * Explicitly separates mandatory (required) and optional data.
 */
export interface CreateCustomerDTO {
  required: {
    firstName: string;
    lastName: string;
    birthDate: string;
    address: Address;
  };
  optional?: CustomerDetails;
}
