import {Helmet} from "react-helmet-async";
import styles from "./About.module.css";

export default function About() {
    return (
        <div className={styles.page}>
            <Helmet>
                <title>About Us - Electronics Store</title>
                <meta name="description" content="Learn about our mission to deliver the latest electronics at the cheapest prices with fast, reliable delivery." />
            </Helmet>
            <h1>About Us</h1>
            <p>
                We are an electronics store dedicated to bringing you the latest tech
                at the cheapest prices — with fast, reliable delivery you can count on.
            </p>
            <p>
                From laptops and phones to headphones and accessories, we source
                products directly so you get the best deals without the wait.
            </p>
            <h2>Our Mission</h2>
            <p>
                Fast delivery. Cheap prices. No compromises. That's what we're about.
            </p>
        </div>
    );
}
