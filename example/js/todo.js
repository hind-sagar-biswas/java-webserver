let todos = [];
let currentFilter = 'all';

// Load todos on page load
document.addEventListener('DOMContentLoaded', () => {
	loadTodos();
	setupEventListeners();
});

function setupEventListeners() {
	// Add todo form
	document.getElementById('addTodoForm').addEventListener('submit', async (e) => {
		e.preventDefault();
		const input = document.getElementById('todoInput');
		const title = input.value.trim();

		if (!title) return;

		await addTodo(title);
		input.value = '';
	});

	// Filter buttons
	document.querySelectorAll('.filter-btn').forEach(btn => {
		btn.addEventListener('click', () => {
			document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
			btn.classList.add('active');
			currentFilter = btn.dataset.filter;
			renderTodos();
		});
	});
}

async function loadTodos() {
	try {
		const response = await fetch('/api/todos');
		if (response.ok) {
			todos = await response.json();
			renderTodos();
		} else {
			showMessage('Failed to load todos', 'error');
		}
	} catch (error) {
		showMessage('Error loading todos', 'error');
	}
}

async function addTodo(title) {
	try {
		const response = await fetch('/api/todos', {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ title })
		});

		if (response.ok) {
			const newTodo = await response.json();
			todos.push(newTodo);
			renderTodos();
			showMessage('Todo added!', 'success');
		} else {
			showMessage('Failed to add todo', 'error');
		}
	} catch (error) {
		showMessage('Error adding todo', 'error');
	}
}

async function toggleTodo(id) {
	try {
		const response = await fetch('/api/todos', {
			method: 'PUT',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ id: id.toString() })
		});

		if (response.ok) {
			const updatedTodo = await response.json();
			const index = todos.findIndex(t => t.id === id);
			if (index !== -1) {
				todos[index] = updatedTodo;
				renderTodos();
			}
		} else {
			showMessage('Failed to update todo', 'error');
		}
	} catch (error) {
		showMessage('Error updating todo', 'error');
	}
}

async function deleteTodo(id) {
	if (!confirm('Are you sure you want to delete this todo?')) return;

	try {
		const response = await fetch('/api/todos', {
			method: 'DELETE',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ id: id.toString() })
		});

		if (response.ok) {
			todos = todos.filter(t => t.id !== id);
			renderTodos();
			showMessage('Todo deleted!', 'success');
		} else {
			showMessage('Failed to delete todo', 'error');
		}
	} catch (error) {
		showMessage('Error deleting todo', 'error');
	}
}

function renderTodos() {
	const todoList = document.getElementById('todoList');

	// Filter todos
	let filteredTodos = todos;
	if (currentFilter === 'active') {
		filteredTodos = todos.filter(t => !t.completed);
	} else if (currentFilter === 'completed') {
		filteredTodos = todos.filter(t => t.completed);
	}

	// Render
	if (filteredTodos.length === 0) {
		const emptyMessage = currentFilter === 'all'
			? 'No todos yet. Add one above!'
			: currentFilter === 'active'
				? 'No active todos. Great job! 🎉'
				: 'No completed todos yet. Check the box to mark as complete!';
		todoList.innerHTML = `<div class="empty-state">${emptyMessage}</div>`;
	} else {
		todoList.innerHTML = filteredTodos.map(todo => `
<div class="todo-item ${todo.completed ? 'completed' : ''}">
<input type="checkbox" 
${todo.completed ? 'checked' : ''} 
onchange="toggleTodo(${todo.id})">
<span class="todo-title">${escapeHtml(todo.title)}</span>
<button class="btn-delete" onclick="deleteTodo(${todo.id})">×</button>
</div>
`).join('');
	}

	// Update stats
	const activeCount = todos.filter(t => !t.completed).length;
	document.getElementById('stats').textContent =
		`${activeCount} ${activeCount === 1 ? 'item' : 'items'} left`;
}

function showMessage(text, type) {
	const message = document.getElementById('message');
	message.textContent = text;
	message.className = 'message ' + type;
	message.classList.remove('hidden');
	setTimeout(() => message.classList.add('hidden'), 3000);
}

function escapeHtml(text) {
	const div = document.createElement('div');
	div.textContent = text;
	return div.innerHTML;
}

