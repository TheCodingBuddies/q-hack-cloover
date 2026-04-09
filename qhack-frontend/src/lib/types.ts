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
  metadata?: Record<string, string>;
}

export interface SunnyScoreResponse {
  address: string;
  sunnyPlace: string;
  explanation: string;
}

export interface PropertyResponseDto {
  id: number;
  postCode: string;
  street: string;
  city: string;
  houseNumber: string;
  sunnyScore?: SunnyScoreResponse;
  metadata?: Record<string, string>;
}

/**
 * Data Transfer Object for customer with properties response from the backend.
 */
export interface CustomerWithPropertiesResponseDto {
  id: number;
  firstName: string;
  lastName: string;
  birthDate: string;
  properties: PropertyResponseDto[];
}

/**
 * Data Transfer Object for customer response from the backend.
 */
export interface CustomerResponseDto {
  id: number;
  firstName: string;
  lastName: string;
  birthDate: string;
  properties?: PropertyResponseDto[];
}

export interface OfferData {
  lead_summary: {
    location: {
      postal_code: string;
      city: string;
      country: string;
    };
    primary_product: string;
    building_assumptions: string[];
  };
  market_context: {
    summary: string;
    drivers: string[];
    why_now: string[];
  };
  subsidies: Array<{
    name: string;
    status: string;
    relevance: string;
    estimated_effect_eur: number;
    notes: string;
  }>;
  recommended_offer: {
    package_name: string;
    products: string[];
    reasoning: string[];
    estimated_cost_range_eur: { min: number; max: number };
    estimated_annual_savings_eur: { min: number; max: number };
    estimated_payback_years: { min: number; max: number };
  };
  alternative_offers: Array<{
    package_name: string;
    products: string[];
    positioning: string;
    estimated_cost_range_eur: { min: number; max: number };
  }>;
  financing_options: Array<{
    scenario: string;
    down_payment_eur: number;
    loan_amount_eur: number;
    term_months: number;
    apr_percent: number;
    monthly_payment_eur: number;
    notes: string;
  }>;
  sales_talking_points: string[];
  missing_information: string[];
  disclaimer: string;
}
