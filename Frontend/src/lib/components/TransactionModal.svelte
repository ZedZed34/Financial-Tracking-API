<script>
    import { transactionsApi } from '$lib/api';

    export let show = false;
    export let onClose = () => {};
    export let onSuccess = () => {};
    export let accounts = [];
    export let categories = [];

    let amount = 0;
    let type = 'EXPENSE';
    let date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    let description = '';
    let accountId = '';
    let categoryId = '';
    let loading = false;
    let error = '';

    // Auto-select first account if available
    $: if (show && accounts.length > 0 && !accountId) {
        accountId = accounts[0].id;
    }

    async function handleSubmit() {
        if (!accountId) {
            error = 'Please select an account';
            return;
        }

        loading = true;
        error = '';
        try {
            await transactionsApi.create({
                amount: parseFloat(amount),
                type,
                date,
                description,
                accountId: parseInt(accountId),
                categoryId: categoryId ? parseInt(categoryId) : null
            });
            onSuccess();
            close();
        } catch (e) {
            error = e.message || 'Failed to create transaction';
        } finally {
            loading = false;
        }
    }

    function close() {
        amount = 0;
        type = 'EXPENSE';
        date = new Date().toISOString().split('T')[0];
        description = '';
        accountId = accounts.length > 0 ? accounts[0].id : '';
        categoryId = '';
        error = '';
        onClose();
    }
</script>

{#if show}
<div class="modal-backdrop">
    <div class="modal-content">
        <h2>Log Transaction</h2>
        {#if error}
            <div class="error-msg">{error}</div>
        {/if}
        <form on:submit|preventDefault={handleSubmit}>
            
            <div class="form-row">
                <div class="form-group flex-1">
                    <label for="txn-type">Type</label>
                    <select id="txn-type" bind:value={type}>
                        <option value="EXPENSE">Expense</option>
                        <option value="INCOME">Income</option>
                    </select>
                </div>
                <div class="form-group flex-1">
                    <label for="txn-amount">Amount ($)</label>
                    <input id="txn-amount" type="number" step="0.01" bind:value={amount} required min="0.01" />
                </div>
            </div>
            
            <div class="form-group">
                <label for="txn-desc">Description</label>
                <input id="txn-desc" type="text" bind:value={description} placeholder="e.g. Groceries at Walmart" required />
            </div>

            <div class="form-row">
                <div class="form-group flex-1">
                    <label for="txn-date">Date</label>
                    <input id="txn-date" type="date" bind:value={date} required />
                </div>
                <div class="form-group flex-1">
                    <label for="txn-account">Account</label>
                    <select id="txn-account" bind:value={accountId} required>
                        {#if accounts.length === 0}
                            <option value="" disabled>No accounts available</option>
                        {/if}
                        {#each accounts as account}
                            <option value={account.id}>{account.name} (${account.balance})</option>
                        {/each}
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label for="txn-category">Category (Optional)</label>
                <select id="txn-category" bind:value={categoryId}>
                    <option value="">-- None --</option>
                    {#each categories as category}
                        <option value={category.id}>{category.name}</option>
                    {/each}
                </select>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" on:click={close} disabled={loading}>Cancel</button>
                <button type="submit" class="btn btn-primary" disabled={loading || accounts.length === 0}>
                    {loading ? 'Saving...' : 'Save Transaction'}
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
        max-width: 450px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
        border: 1px solid rgba(255,255,255,0.1);
        animation: scaleIn 0.2s ease-out;
    }
    @keyframes scaleIn {
        from { transform: scale(0.95); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
    h2 { margin-top: 0; margin-bottom: 1.5rem; color: var(--text); }
    .form-group { margin-bottom: 1rem; }
    .form-row { display: flex; gap: 1rem; }
    .flex-1 { flex: 1; }
    
    label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-muted); }
    input, select {
        width: 100%; padding: 0.75rem; border-radius: 8px;
        background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);
        color: var(--text); box-sizing: border-box;
    }
    input:focus, select:focus { outline: none; border-color: var(--primary); }
    
    .error-msg {
        background: rgba(239, 68, 68, 0.1); color: #ef4444;
        padding: 0.75rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.875rem;
    }
    .modal-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2rem; }
    .btn-secondary { background: transparent; border: 1px solid rgba(255,255,255,0.2); }
    .btn-secondary:hover { background: rgba(255,255,255,0.05); }
    button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
