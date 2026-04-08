export interface Address {
  zip: string;
  city: string;
  street: string;
  houseNumber: string;
}

export interface Customer {
  firstName: string;
  lastName: string;
  birthDate: string;
  address: Address;
}
