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
 * Property as returned inside a customer response from the backend.
 */
export interface PropertyDto {
  postCode: string;
  street: string;
  city: string;
  houseNumber: string;
  sunnyScore?: number | null;
}

/**
 * Full customer response from the backend, including optional property.
 * Matches: GET /customers and GET /customer/{id}
 */
export interface CustomerWithPropertyResponseDto {
  id: number;
  firstName: string;
  lastName: string;
  birthDate: string | null;
  property: PropertyDto | null;
}
