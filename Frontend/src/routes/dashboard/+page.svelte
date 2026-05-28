<script>
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import { userApi, accountsApi, categoriesApi, transactionsApi } from '$lib/api';

    let user = $state(null);
    let accounts = $state([]);
    let categories = $state([]);
    let transactions = $state([]);
    let loading = $state(true);
    let errorMsg = $state('');

    onMount(async () => {
        if (!localStorage.getItem('jwt_token')) {
            goto('/');
            return;
        }
        await loadData();
    });

    async function loadData() {
        try {
            loading = true;
            user = await userApi.getMe();
            
            // Get dates for the last 30 days
            const d = new Date();
            const end = d.toISOString().split('T')[0];
            d.setDate(d.getDate() - 30);
            const start = d.toISOString().split('T')[0];
            
            // Fetch everything in parallel
            const [accRes, catRes, txnRes] = await Promise.all([
                accountsApi.getAll(),
                categoriesApi.getAll(),
                transactionsApi.getAll(`?start=${start}&end=${end}`)
            ]);
            
            accounts = accRes;
            categories = catRes;
            transactions = txnRes.content || [];
        } catch (err) {
            errorMsg = err.message || 'Failed to load dashboard data';
        } finally {
            loading = false;
        }
    }

    function logout() {
        localStorage.removeItem('jwt_token');
        goto('/');
    }

    // Helper to format currency
    function formatCurrency(amount) {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
    }
</script>

<svelte:head>
    <title>Dashboard | Financial App</title>
</svelte:head>

{#if loading}
    <div class="loading-state">
        <div class="spinner"></div>
        <p>Loading your financial data...</p>
    </div>
{:else if errorMsg}
    <div class="dashboard-wrapper">
        <div class="alert alert-error">
            {errorMsg}
            <button class="btn btn-primary" style="margin-left: 15px;" onclick={loadData}>Retry</button>
            <button class="btn btn-secondary" style="margin-left: 15px;" onclick={logout}>Log Out</button>
        </div>
    </div>
{:else}
    <div class="dashboard-wrapper">
        <header class="top-nav glass-card">
            <div class="nav-content">
                <h2>FinTrack</h2>
                <div class="user-menu">
                    <span class="user-greeting">Hello, <strong>{user?.name || 'User'}</strong></span>
                    <button class="btn btn-secondary btn-sm" onclick={logout}>Sign Out</button>
                </div>
            </div>
        </header>

        <main class="dashboard-grid">
            <!-- Accounts Section -->
            <section class="dashboard-section">
                <div class="section-header">
                    <h3>Your Accounts</h3>
                    <button class="btn btn-primary btn-sm">+ Add</button>
                </div>
                <div class="accounts-list">
                    {#each accounts as account}
                        <div class="account-card glass-card">
                            <div class="acc-info">
                                <h4>{account.name}</h4>
                                <span class="acc-type badge">{account.type}</span>
                            </div>
                            <div class="acc-balance">
                                {formatCurrency(account.balance)}
                            </div>
                        </div>
                    {:else}
                        <div class="empty-state glass-card">
                            <p>No accounts yet. Create one to start tracking!</p>
                        </div>
                    {/each}
                </div>
            </section>

            <!-- Transactions Section -->
            <section class="dashboard-section main-content">
                <div class="section-header">
                    <h3>Recent Transactions</h3>
                    <button class="btn btn-primary btn-sm">+ New Transaction</button>
                </div>
                <div class="glass-card table-container">
                    {#if transactions.length > 0}
                        <table class="transactions-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Description</th>
                                    <th>Category</th>
                                    <th class="text-right">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each transactions as txn}
                                    <tr>
                                        <td>{new Date(txn.date).toLocaleDateString()}</td>
                                        <td>{txn.description}</td>
                                        <td><span class="badge badge-outline">{txn.category?.name || 'Uncategorized'}</span></td>
                                        <td class="text-right amount {txn.type === 'INCOME' ? 'positive' : 'negative'}">
                                            {txn.type === 'INCOME' ? '+' : '-'}{formatCurrency(txn.amount)}
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    {:else}
                        <div class="empty-state">
                            <p>No transactions found.</p>
                        </div>
                    {/if}
                </div>
            </section>
        </main>
    </div>
{/if}

<style>
    .dashboard-wrapper {
        padding: 20px;
        max-width: 1200px;
        margin: 0 auto;
        width: 100%;
        display: flex;
        flex-direction: column;
        gap: 30px;
    }

    .top-nav {
        padding: 16px 24px;
        margin-top: 10px;
    }

    .nav-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .nav-content h2 {
        margin: 0;
        color: var(--accent-primary);
        font-weight: 700;
        letter-spacing: -1px;
    }

    .user-menu {
        display: flex;
        align-items: center;
        gap: 20px;
    }

    .user-greeting {
        color: var(--text-secondary);
    }

    .user-greeting strong {
        color: var(--text-primary);
    }

    .btn-sm {
        padding: 8px 16px;
        font-size: 0.875rem;
    }

    .dashboard-grid {
        display: grid;
        grid-template-columns: 300px 1fr;
        gap: 24px;
        align-items: start;
    }

    @media (max-width: 800px) {
        .dashboard-grid {
            grid-template-columns: 1fr;
        }
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
    }

    .section-header h3 {
        margin: 0;
        font-size: 1.25rem;
    }

    .accounts-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .account-card {
        padding: 16px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        transition: transform 0.2s, box-shadow 0.2s;
    }

    .account-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 20px -8px rgba(0,0,0,0.4);
    }

    .acc-info h4 {
        margin: 0 0 4px 0;
        font-size: 1rem;
    }

    .badge {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 99px;
        font-size: 0.75rem;
        font-weight: 600;
        background-color: rgba(255, 255, 255, 0.1);
        color: var(--text-secondary);
    }

    .badge-outline {
        background: transparent;
        border: 1px solid var(--border-color);
    }

    .acc-balance {
        font-size: 1.15rem;
        font-weight: 600;
        color: var(--text-primary);
    }

    .table-container {
        padding: 0;
        overflow: hidden;
    }

    .transactions-table {
        width: 100%;
        border-collapse: collapse;
        text-align: left;
    }

    .transactions-table th, .transactions-table td {
        padding: 16px;
        border-bottom: 1px solid var(--border-color);
    }

    .transactions-table th {
        font-size: 0.85rem;
        text-transform: uppercase;
        letter-spacing: 1px;
        color: var(--text-secondary);
        font-weight: 600;
        background-color: rgba(0, 0, 0, 0.2);
    }

    .transactions-table tr:last-child td {
        border-bottom: none;
    }

    .transactions-table tbody tr:hover {
        background-color: rgba(255, 255, 255, 0.02);
    }

    .text-right {
        text-align: right;
    }

    .amount {
        font-weight: 600;
        font-variant-numeric: tabular-nums;
    }

    .positive {
        color: var(--success);
    }

    .negative {
        color: var(--text-primary);
    }

    .loading-state, .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px;
        color: var(--text-secondary);
        text-align: center;
        height: 100%;
        min-height: 200px;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid rgba(255,255,255,0.1);
        border-radius: 50%;
        border-top-color: var(--accent-primary);
        animation: spin 1s ease-in-out infinite;
        margin-bottom: 16px;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }
</style>
