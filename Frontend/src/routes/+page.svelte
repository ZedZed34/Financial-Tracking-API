<script>
    import { authApi } from '$lib/api';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';

    let isLoginMode = $state(true);
    let loading = $state(false);
    let errorMsg = $state('');

    // Form fields
    let name = $state('');
    let email = $state('');
    let password = $state('');

    onMount(() => {
        // If already logged in, redirect to dashboard
        if (localStorage.getItem('jwt_token')) {
            goto('/dashboard');
        }
    });

    async function handleSubmit(event) {
        event.preventDefault();
        loading = true;
        errorMsg = '';
        
        try {
            if (isLoginMode) {
                const response = await authApi.login(email, password);
                localStorage.setItem('jwt_token', response.token);
                goto('/dashboard');
            } else {
                await authApi.register(name, email, password);
                // Switch to login after successful register
                isLoginMode = true;
                errorMsg = 'Registration successful! Please log in.';
                password = ''; // Clear password field
            }
        } catch (err) {
            errorMsg = err.message || 'An error occurred';
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>{isLoginMode ? 'Login' : 'Sign Up'} | Financial App</title>
</svelte:head>

<div class="auth-wrapper">
    <div class="auth-card glass-card">
        <div class="auth-header">
            <h1>{isLoginMode ? 'Welcome back' : 'Create an account'}</h1>
            <p>{isLoginMode ? 'Enter your details to access your dashboard.' : 'Start tracking your finances today.'}</p>
        </div>

        {#if errorMsg}
            <div class="alert {errorMsg.includes('successful') ? 'alert-success' : 'alert-error'}">
                {errorMsg}
            </div>
        {/if}

        <form onsubmit={handleSubmit}>
            {#if !isLoginMode}
                <div class="input-group">
                    <label class="input-label" for="name">Full Name</label>
                    <input class="input-field" type="text" id="name" bind:value={name} required placeholder="John Doe" />
                </div>
            {/if}

            <div class="input-group">
                <label class="input-label" for="email">Email</label>
                <input class="input-field" type="email" id="email" bind:value={email} required placeholder="you@example.com" />
            </div>

            <div class="input-group">
                <label class="input-label" for="password">Password</label>
                <input class="input-field" type="password" id="password" bind:value={password} required placeholder="••••••••" />
            </div>

            <button type="submit" class="btn btn-primary w-100" disabled={loading}>
                {loading ? 'Processing...' : (isLoginMode ? 'Log In' : 'Sign Up')}
            </button>
        </form>

        <div class="auth-footer">
            <p>
                {isLoginMode ? "Don't have an account?" : 'Already have an account?'}
                <button class="text-btn" type="button" onclick={() => { isLoginMode = !isLoginMode; errorMsg = ''; }}>
                    {isLoginMode ? 'Sign up' : 'Log in'}
                </button>
            </p>
        </div>
    </div>
</div>

<style>
    .auth-wrapper {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 20px;
        background: radial-gradient(circle at top right, rgba(59, 130, 246, 0.15), transparent 40%),
                    radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.1), transparent 40%);
    }

    .auth-card {
        width: 100%;
        max-width: 420px;
        position: relative;
        overflow: hidden;
    }

    /* Subtle glow behind card */
    .auth-card::before {
        content: '';
        position: absolute;
        top: -50%; left: -50%;
        width: 200%; height: 200%;
        background: conic-gradient(from 0deg, transparent 0%, rgba(59, 130, 246, 0.1) 25%, transparent 50%);
        animation: rotate 10s linear infinite;
        z-index: -1;
    }

    @keyframes rotate {
        100% { transform: rotate(360deg); }
    }

    .auth-header {
        text-align: center;
        margin-bottom: 32px;
    }

    .auth-header h1 {
        font-size: 1.75rem;
        margin-bottom: 8px;
    }

    .auth-header p {
        font-size: 0.95rem;
        margin: 0;
    }

    .w-100 {
        width: 100%;
        margin-top: 10px;
    }

    .auth-footer {
        margin-top: 24px;
        text-align: center;
        font-size: 0.9rem;
    }

    .text-btn {
        background: none;
        border: none;
        color: var(--accent-primary);
        font-weight: 500;
        cursor: pointer;
        padding: 0 4px;
        font-family: inherit;
        font-size: inherit;
    }
    
    .text-btn:hover {
        color: var(--accent-hover);
        text-decoration: underline;
    }

    .alert {
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 24px;
        font-size: 0.9rem;
        text-align: center;
    }

    .alert-error {
        background-color: rgba(239, 68, 68, 0.1);
        color: #fca5a5;
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .alert-success {
        background-color: rgba(16, 185, 129, 0.1);
        color: #6ee7b7;
        border: 1px solid rgba(16, 185, 129, 0.2);
    }
</style>
