<script>
    import { accountsApi } from '$lib/api';

    let { show = false, onClose = () => {}, onSuccess = () => {} } = $props();

    let name = $state('');
    let type = $state('CHECKING');
    let balance = $state(0);
    let loading = $state(false);
    let error = $state('');

    async function handleSubmit() {
        loading = true;
        error = '';
        try {
            await accountsApi.create({
                name,
                type,
                balance: parseFloat(balance)
            });
            onSuccess();
            close();
        } catch (e) {
            error = e.message || 'Failed to create account';
        } finally {
            loading = false;
        }
    }

    function close() {
        name = '';
        type = 'CHECKING';
        balance = 0;
        error = '';
        onClose();
    }
</script>

{#if show}
<div class="modal-backdrop">
    <div class="modal-content">
        <h2>Add New Account</h2>
        {#if error}
            <div class="error-msg">{error}</div>
        {/if}
        <form on:submit|preventDefault={handleSubmit}>
            <div class="form-group">
                <label for="acc-name">Account Name</label>
                <input id="acc-name" type="text" bind:value={name} placeholder="e.g. Chase Checking" required />
            </div>
            
            <div class="form-group">
                <label for="acc-type">Account Type</label>
                <select id="acc-type" bind:value={type}>
                    <option value="CHECKING">Checking</option>
                    <option value="SAVINGS">Savings</option>
                    <option value="CREDIT">Credit</option>
                    <option value="INVESTMENT">Investment</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="acc-balance">Initial Balance ($)</label>
                <input id="acc-balance" type="number" step="0.01" bind:value={balance} required />
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" on:click={close} disabled={loading}>Cancel</button>
                <button type="submit" class="btn btn-primary" disabled={loading}>
                    {loading ? 'Saving...' : 'Save Account'}
                </button>
            </div>
        </form>
    </div>
</div>
{/if}

<style>
    .modal-backdrop {
        position: fixed;
        top: 0; left: 0; width: 100vw; height: 100vh;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(4px);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    .modal-content {
        background: var(--surface);
        padding: 2rem;
        border-radius: 16px;
        width: 100%;
        max-width: 400px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
        border: 1px solid rgba(255,255,255,0.1);
        animation: scaleIn 0.2s ease-out;
    }
    @keyframes scaleIn {
        from { transform: scale(0.95); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
    h2 {
        margin-top: 0;
        margin-bottom: 1.5rem;
        color: var(--text);
    }
    .form-group {
        margin-bottom: 1rem;
    }
    label {
        display: block;
        margin-bottom: 0.5rem;
        font-size: 0.875rem;
        color: var(--text-muted);
    }
    input, select {
        width: 100%;
        padding: 0.75rem;
        border-radius: 8px;
        background: rgba(255,255,255,0.05);
        border: 1px solid rgba(255,255,255,0.1);
        color: var(--text);
        box-sizing: border-box;
    }
    input:focus, select:focus {
        outline: none;
        border-color: var(--primary);
    }
    .error-msg {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
        padding: 0.75rem;
        border-radius: 8px;
        margin-bottom: 1rem;
        font-size: 0.875rem;
    }
    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
        margin-top: 2rem;
    }
    .btn-secondary {
        background: transparent;
        border: 1px solid rgba(255,255,255,0.2);
    }
    .btn-secondary:hover {
        background: rgba(255,255,255,0.05);
    }
</style>
