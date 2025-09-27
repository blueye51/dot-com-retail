import styles from './Login.module.css';
import {useState} from "react";

function Login() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    async function handleSubmit(event) {
        console.log('submitted');
        event.preventDefault();
        try{
            const res = await fetch('https://localhost:8443/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });
            const data = await res.json();
            console.log(data);

        } catch (error) {
            console.error('Error during login:', error);
        }
    }


    return (
        <div className={styles.loginContainer}>
            <h2>Login</h2>
            <form onSubmit={handleSubmit} className={styles.loginForm}>
                <div className={styles.formField}>
                    <label htmlFor="email">Email:</label>
                    <input
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Email"
                        type="email"
                        id="email"
                        name="email"
                        inputMode="email"
                        autoComplete="email"
                        value={email}
                        required
                    />
                </div>
                <div className={styles.formField}>
                    <label htmlFor="password">Password:</label>
                    <input
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Password"
                        type="password"
                        id="password"
                        name="password"
                        autoComplete="current-password"
                        value={password}
                        required
                    />
                </div>
                <button type="submit" className={styles.loginButton}>Login</button>
            </form>
        </div>
    );
}

export default Login;