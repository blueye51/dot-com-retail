import styles from './Checkout.module.css';
import {Helmet} from "react-helmet-async";
import {useState, useEffect, useCallback} from "react";
import {useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";
import {paths} from "../routes.js";
import {loadStripe} from "@stripe/stripe-js";
import {Elements, PaymentElement, useStripe, useElements} from "@stripe/react-stripe-js";
import AddressAutocomplete from "./AddressAutocomplete.jsx";

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);
const BASE_URL = import.meta.env.VITE_API_BASE;

function CheckoutForm({orderId}) {
    const stripe = useStripe();
    const elements = useElements();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!stripe || !elements) return;

        setLoading(true);
        setError(null);

        const {error: submitError} = await elements.submit();
        if (submitError) {
            setError(submitError.message);
            setLoading(false);
            return;
        }

        const {error: confirmError} = await stripe.confirmPayment({
            elements,
            confirmParams: {
                return_url: `${window.location.origin}${paths.orderConfirmation(orderId)}`,
            },
        });

        if (confirmError) {
            setError(confirmError.message);
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className={styles.paymentForm}>
            <PaymentElement/>
            <p className={styles.testCards}>
                Test cards: <code>4242 4242 4242 4242</code> (success),{" "}
                <code>4000 0000 0000 9995</code> (insufficient funds),{" "}
                <code>4000 0000 0000 0069</code> (expired). Use any future date and any CVC.
            </p>
            {error && <p className={styles.error}>{error}</p>}
            <button type="submit" disabled={!stripe || loading} className={styles.payBtn}>
                {loading ? "Processing..." : "Pay Now"}
            </button>
        </form>
    );
}

export default function Checkout() {
    const {token} = useSelector((s) => s.auth);
    const navigate = useNavigate();

    const [address, setAddress] = useState({
        name: "",
        addressLine1: "",
        addressLine2: "",
        city: "",
        state: "",
        zip: "",
        country: "",
    });
    const [shippingOption, setShippingOption] = useState("STANDARD");
    const [saveAddress, setSaveAddress] = useState(false);
    const [addressConfirmed, setAddressConfirmed] = useState(false);
    const [clientSecret, setClientSecret] = useState(null);
    const [orderId, setOrderId] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!token) {
            navigate(paths.login(), {replace: true});
            return;
        }
        // Load saved address
        fetch(`${BASE_URL}/api/users/me/address`, {
            headers: {Authorization: `Bearer ${token}`},
            credentials: "include",
        })
            .then((res) => res.ok ? res.json() : null)
            .then((data) => {
                if (data) {
                    setAddress({
                        name: data.name || "",
                        addressLine1: data.addressLine1 || "",
                        addressLine2: data.addressLine2 || "",
                        city: data.city || "",
                        state: data.state || "",
                        zip: data.zip || "",
                        country: data.country || "",
                    });
                }
            })
            .catch(() => {});
    }, [token, navigate]);

    const handleAutocompleteSelect = useCallback((place) => {
        setAddress((prev) => ({
            ...prev,
            addressLine1: place.address,
            city: place.city,
            state: place.state,
            zip: place.zip,
            country: place.country,
        }));
    }, []);

    const handleFieldChange = (field) => (e) => {
        setAddress((prev) => ({...prev, [field]: e.target.value}));
    };

    const isAddressValid = address.name.trim()
        && address.addressLine1.trim()
        && address.city.trim()
        && address.state.trim()
        && address.zip.trim()
        && address.country.trim();

    const handleAddressSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const res = await fetch(`${BASE_URL}/api/orders/checkout`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify({...address, saveAddress, shippingOption}),
            });
            if (!res.ok) {
                const text = await res.text();
                throw new Error(text || `HTTP ${res.status}`);
            }
            const data = await res.json();
            setClientSecret(data.clientSecret);
            setOrderId(data.orderId);
            setAddressConfirmed(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (addressConfirmed && clientSecret) {
        return (
            <div className={styles.page}>
                <h1>Payment</h1>
                <div className={styles.addressSummary}>
                    <p><strong>{address.name}</strong></p>
                    <p>{address.addressLine1}{address.addressLine2 ? `, ${address.addressLine2}` : ""}</p>
                    <p>{address.city}, {address.state} {address.zip}</p>
                    <p>{address.country}</p>
                    <p className={styles.shippingLabel}>
                        {shippingOption === "EXPRESS" ? "Express Shipping — $14.99" : "Standard Shipping — $4.99"}
                    </p>
                </div>
                <Elements stripe={stripePromise} options={{clientSecret}}>
                    <CheckoutForm orderId={orderId}/>
                </Elements>
            </div>
        );
    }

    return (
        <div className={styles.page}>
            <Helmet>
                <title>Checkout - Electronics Store</title>
                <meta name="description" content="Complete your purchase with secure payment processing." />
            </Helmet>
            <h1>Shipping Address</h1>
            <form onSubmit={handleAddressSubmit} className={styles.addressForm}>
                <label className={styles.field}>
                    <span>Full Name</span>
                    <input type="text" value={address.name}
                           onChange={handleFieldChange("name")} required/>
                </label>

                <label className={styles.field}>
                    <span>Search Address</span>
                    <AddressAutocomplete
                        onAddressSelect={handleAutocompleteSelect}
                        className={styles.input}
                    />
                </label>

                <label className={styles.field}>
                    <span>Street Address</span>
                    <input type="text" value={address.addressLine1}
                           onChange={handleFieldChange("addressLine1")} required/>
                </label>

                <label className={styles.field}>
                    <span>Apt / Suite (optional)</span>
                    <input type="text" value={address.addressLine2}
                           onChange={handleFieldChange("addressLine2")}/>
                </label>

                <div className={styles.row}>
                    <label className={styles.field}>
                        <span>City</span>
                        <input type="text" value={address.city}
                               onChange={handleFieldChange("city")} required/>
                    </label>
                    <label className={styles.field}>
                        <span>State</span>
                        <input type="text" value={address.state}
                               onChange={handleFieldChange("state")} required/>
                    </label>
                </div>

                <div className={styles.row}>
                    <label className={styles.field}>
                        <span>ZIP Code</span>
                        <input type="text" value={address.zip}
                               onChange={handleFieldChange("zip")} required/>
                    </label>
                    <label className={styles.field}>
                        <span>Country</span>
                        <input type="text" value={address.country}
                               onChange={handleFieldChange("country")} required/>
                    </label>
                </div>

                <fieldset className={styles.shippingOptions}>
                    <legend>Shipping Method</legend>
                    <label className={styles.radioRow}>
                        <input type="radio" name="shipping" value="STANDARD"
                               checked={shippingOption === "STANDARD"}
                               onChange={() => setShippingOption("STANDARD")}/>
                        <span>Standard Shipping (5-7 days) — $4.99</span>
                    </label>
                    <label className={styles.radioRow}>
                        <input type="radio" name="shipping" value="EXPRESS"
                               checked={shippingOption === "EXPRESS"}
                               onChange={() => setShippingOption("EXPRESS")}/>
                        <span>Express Shipping (2-3 days) — $14.99</span>
                    </label>
                </fieldset>

                <label className={styles.checkboxRow}>
                    <input type="checkbox" checked={saveAddress}
                           onChange={(e) => setSaveAddress(e.target.checked)}/>
                    <span>Save this address for next time</span>
                </label>

                {error && <p className={styles.error}>{error}</p>}

                <button type="submit" disabled={!isAddressValid || loading} className={styles.payBtn}>
                    {loading ? "Creating order..." : "Continue to Payment"}
                </button>
            </form>
        </div>
    );
}
