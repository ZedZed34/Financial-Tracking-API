<script>
	import '../app.css';
	let { children } = $props();
</script>

<div class="app-container">
	{@render children()}
</div>

<style>
	.app-container {
		width: 100%;
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}
</style>
