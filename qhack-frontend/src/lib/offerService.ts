import type { OfferLLMRequest, OfferLLMResponse } from './types';

export const offerService = {
  async generateOffer(request: OfferLLMRequest): Promise<OfferLLMResponse> {
    const response = await fetch('http://localhost:8080/offers', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(request)
    });

    if (response.ok) {
      return await response.json();
    }

    throw new Error(`Failed to generate offer: ${response.status}`);
  }
};
