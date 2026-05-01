# Payment Service - Stripe Integration

This service handles payment processing using Stripe for the Order Processing System.

## Setup

### Environment Variables

```bash
export STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key_here
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
export BASE_URL=http://localhost:8080/
```

### Stripe Configuration and testing

1. Create a Stripe account at https://stripe.com
2. Get your secret key from the Stripe dashboard add in the env or application.yml
3. I used localhost so download stripe CLI
4. Download the stripe cli from here: https://docs.stripe.com/stripe-cli/install?install-method=windows
5. Un-zip adn open the cmd where stripe.exe ius located
6. check : stripe -version
7. login : stripe login
8. Use Stripe CLI for webhook testing:
   stripe listen --forward-to localhost:8080/api/v1/payments/webhook/stripe
9. You have to copy the webhook secret from here and in env or application.yml
10. Use the /create-checkout-session get the url orthe session ID
11. Paste in another tab fill info
12. After successful payemnt or process you will be looking inthe logs teh stripe called the
    webhook url

### Sample Test Data

- Test card number: 4242 4242 4242 4242
- Any future expiry date and CVC

Use Stripe test cards:
- Success: 4242 4242 4242 4242
- Failure: 4000 0000 0000 0002