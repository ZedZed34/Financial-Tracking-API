const API_BASE_URL = import.meta.env.PROD ? '/api' : 'http://localhost:3000/api';

/**
 * Helper function to handle API requests with automatic JWT injection
 */
export async function fetchApi(endpoint, options = {}) {
    const defaultHeaders = {
        'Content-Type': 'application/json',
    };

    const config = {
        ...options,
        credentials: 'include', // Automatically send HttpOnly cookies
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        
        // Handle 401 Unauthorized (Token expired or invalid)
        if (response.status === 401) {
            if (window.location.pathname !== '/') {
                window.location.href = '/';
            }
            throw new Error('Unauthorized');
        }

        // If it's a 204 No Content, return null
        if (response.status === 204) return null;

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(data.error || data.message || 'API Request Failed');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Auth API Methods
export const authApi = {
    login: (email, password) => fetchApi('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    }),
    register: (name, email, password) => fetchApi('/auth/signup', {
        method: 'POST',
        body: JSON.stringify({ name, email, password })
    })
};

// User API Methods
export const userApi = {
    getMe: () => fetchApi('/users/me')
};

// Accounts API Methods
export const accountsApi = {
    getAll: () => fetchApi('/accounts'),
    create: (accountData) => fetchApi('/accounts', {
        method: 'POST',
        body: JSON.stringify(accountData)
    })
};

// Categories API Methods
export const categoriesApi = {
    getAll: () => fetchApi('/categories'),
    create: (categoryData) => fetchApi('/categories', {
        method: 'POST',
        body: JSON.stringify(categoryData)
    })
};

// Transactions API Methods
export const transactionsApi = {
    getAll: (params = '') => fetchApi(`/transactions${params}`),
    create: (transactionData) => fetchApi('/transactions', {
        method: 'POST',
        body: JSON.stringify(transactionData)
    })
};
