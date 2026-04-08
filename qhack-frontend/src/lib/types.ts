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
  id: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  address?: Address;
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

/**
 * Data Transfer Object for property details as expected by the backend.
 */
export interface PropertyRequestDto {
  postCode: string;
  street: string;
  city: string;
  houseNumber: string;
}

/**
 * Data Transfer Object for customer response from the backend.
 */
export interface CustomerResponseDto {
  id: number;
  firstName: string;
  lastName: string;
  birthDate: string;
}
