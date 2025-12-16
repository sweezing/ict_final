const API_BASE = 'http://localhost:3000/api';

let currentDb = 'postgres';

// Initialize
$(document).ready(function() {
    checkCurrentDb();
    setupEventListeners();
    loadCardsInfo();
});

function checkCurrentDb() {
    $.get(`${API_BASE}/db-type`)
        .done(function(data) {
            currentDb = data.dbType;
            $('#current-db').text(data.dbType === 'postgres' ? 'PostgreSQL' : 'MongoDB');
        });
}

function setupEventListeners() {
    // Database switching
    $('#switch-postgres').click(() => switchDatabase('postgres'));
    $('#switch-mongo').click(() => switchDatabase('mongo'));

    // Card Users
    $('#create-user').click(createUser);
    $('#update-user').click(updateUser);
    $('#delete-user').click(deleteUser);
    $('#load-users').click(loadUsers);

    // Cards
    $('#create-card').click(createCard);
    $('#update-card').click(updateCard);
    $('#delete-card').click(deleteCard);
    $('#load-cards').click(loadCards);

    // Money operations
    $('#transfer-money').click(transferMoney);
    $('#withdraw-money').click(withdrawMoney);
    $('#deposit-money').click(depositMoney);

    // Cards info
    $('#refresh-cards-info').click(loadCardsInfo);
}

function switchDatabase(dbType) {
    $.ajax({
        url: `${API_BASE}/switch-db`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ dbType }),
        success: function(data) {
            currentDb = dbType;
            $('#current-db').text(dbType === 'postgres' ? 'PostgreSQL' : 'MongoDB');
            showMessage('Database switched to ' + (dbType === 'postgres' ? 'PostgreSQL' : 'MongoDB'), 'success');
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error switching database: ' + xhr.responseJSON?.error, 'error');
        }
    });
}

// Card Users CRUD
function createUser() {
    const name = $('#user-name').val();
    const surname = $('#user-surname').val();
    const iin = $('#user-iin').val();

    if (!name || !surname || !iin) {
        showMessage('Please fill all fields', 'error');
        return;
    }

    $.ajax({
        url: `${API_BASE}/card-users`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ name, surname, iin }),
        success: function(data) {
            showMessage('User created successfully', 'success');
            $('#user-name, #user-surname, #user-iin').val('');
            loadUsers();
        },
        error: function(xhr) {
            showMessage('Error creating user: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function updateUser() {
    const iin = $('#update-user-iin').val();
    const name = $('#update-user-name').val();
    const surname = $('#update-user-surname').val();

    if (!iin || !name || !surname) {
        showMessage('Please fill all fields', 'error');
        return;
    }

    $.ajax({
        url: `${API_BASE}/card-users/${iin}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({ name, surname }),
        success: function(data) {
            showMessage('User updated successfully', 'success');
            $('#update-user-iin, #update-user-name, #update-user-surname').val('');
            loadUsers();
        },
        error: function(xhr) {
            showMessage('Error updating user: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function deleteUser() {
    const iin = $('#delete-user-iin').val();

    if (!iin) {
        showMessage('Please enter IIN', 'error');
        return;
    }

    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    $.ajax({
        url: `${API_BASE}/card-users/${iin}`,
        method: 'DELETE',
        success: function(data) {
            showMessage('User deleted successfully', 'success');
            $('#delete-user-iin').val('');
            loadUsers();
        },
        error: function(xhr) {
            showMessage('Error deleting user: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function loadUsers() {
    $.get(`${API_BASE}/card-users`)
        .done(function(data) {
            let html = '<h3>All Users:</h3>';
            if (data.length === 0) {
                html += '<p>No users found</p>';
            } else {
                data.forEach(user => {
                    html += `<div class="user-item">
                        <strong>Name:</strong> ${user.name} ${user.surname}<br>
                        <strong>IIN:</strong> ${user.iin}
                    </div>`;
                });
            }
            $('#users-list').html(html);
        })
        .fail(function(xhr) {
            showMessage('Error loading users: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        });
}

// Cards CRUD
function createCard() {
    const pan = $('#card-pan').val();
    const cvv = $('#card-cvv').val() || Card.generateCVV();
    const iin = $('#card-iin').val();
    const currency = $('#card-currency').val();
    const balance = parseFloat($('#card-balance').val());

    if (!pan || !iin || !currency || isNaN(balance)) {
        showMessage('Please fill all required fields', 'error');
        return;
    }

    if (pan.length !== 16 || !/^\d+$/.test(pan)) {
        showMessage('PAN must be 16 digits', 'error');
        return;
    }

    if (iin.length !== 12 || !/^\d+$/.test(iin)) {
        showMessage('IIN must be 12 digits', 'error');
        return;
    }

    // Generate expire date (YY/MM format, current + 1 year)
    const now = new Date();
    const expireYear = (now.getFullYear() % 100) + 1;
    const expireMonth = String(now.getMonth() + 1).padStart(2, '0');
    const dateOfExpire = `${expireYear}/${expireMonth}`;

    $.ajax({
        url: `${API_BASE}/cards`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            pan,
            cvv: cvv || Card.generateCVV(),
            dateOfExpire,
            iin,
            currency,
            balance
        }),
        success: function(data) {
            showMessage('Card created successfully', 'success');
            $('#card-pan, #card-cvv, #card-iin, #card-currency, #card-balance').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error creating card: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function updateCard() {
    const id = $('#update-card-id').val();
    const pan = $('#update-card-pan').val();
    const cvv = $('#update-card-cvv').val();
    const name = $('#update-card-name').val();
    const surname = $('#update-card-surname').val();
    const currency = $('#update-card-currency').val();
    const balanceRaw = $('#update-card-balance').val();
    const balance = balanceRaw === '' ? undefined : parseFloat(balanceRaw);

    if (!id) {
        showMessage('Please enter Card ID to update', 'error');
        return;
    }

    // At least one field must be provided
    if (!pan && !cvv && !name && !surname && !currency && balance === undefined) {
        showMessage('Please fill at least one field to update', 'error');
        return;
    }

    const payload = {};
    if (pan) payload.pan = pan;
    if (cvv) payload.cvv = cvv;
    if (name) payload.name = name;
    if (surname) payload.surname = surname;
    if (currency) payload.currency = currency;
    if (balance !== undefined && !isNaN(balance)) payload.balance = balance;

    $.ajax({
        url: `${API_BASE}/cards/${id}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        success: function(data) {
            showMessage('Card updated successfully', 'success');
            $('#update-card-id, #update-card-pan, #update-card-cvv, #update-card-name, #update-card-surname, #update-card-currency, #update-card-balance').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error updating card: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function deleteCard() {
    const id = $('#delete-card-id').val();

    if (!id) {
        showMessage('Please enter Card ID', 'error');
        return;
    }

    if (!confirm('Are you sure you want to delete this card?')) {
        return;
    }

    $.ajax({
        url: `${API_BASE}/cards/${id}`,
        method: 'DELETE',
        success: function(data) {
            showMessage('Card deleted successfully', 'success');
            $('#delete-card-id').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error deleting card: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function loadCards() {
    $.get(`${API_BASE}/cards`)
        .done(function(data) {
            let html = '<h3>All Cards:</h3>';
            if (data.length === 0) {
                html += '<p>No cards found</p>';
            } else {
                data.forEach(card => {
                    const cardId = card.card_id || card._id || 'N/A';
                    html += `<div class="card-item">
                        <strong>ID:</strong> ${cardId}<br>
                        <strong>PAN:</strong> ${card.pan}<br>
                        <strong>CVV:</strong> ${card.cvv}<br>
                        <strong>Name:</strong> ${card.name} ${card.surname}<br>
                        <strong>Currency:</strong> ${card.currency}<br>
                        <strong>Balance:</strong> ${card.balance}<br>
                        <strong>Expire:</strong> ${card.date_of_expire || card.dateOfExpire}
                    </div>`;
                });
            }
            $('#cards-list').html(html);
        })
        .fail(function(xhr) {
            showMessage('Error loading cards: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        });
}

// Money Operations
function transferMoney() {
    const fromPan = $('#transfer-from').val();
    const toPan = $('#transfer-to').val();
    const amount = parseFloat($('#transfer-amount').val());

    if (!fromPan || !toPan || isNaN(amount) || amount <= 0) {
        showMessage('Please fill all fields with valid values', 'error');
        return;
    }

    $.ajax({
        url: `${API_BASE}/cards/transfer`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ fromPan, toPan, amount }),
        success: function(data) {
            showMessage('Money transferred successfully', 'success');
            $('#transfer-from, #transfer-to, #transfer-amount').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error transferring money: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function withdrawMoney() {
    const pan = $('#withdraw-pan').val();
    const cvv = $('#withdraw-cvv').val();
    const amount = parseFloat($('#withdraw-amount').val());

    if (!pan || !cvv || isNaN(amount) || amount <= 0) {
        showMessage('Please fill all fields with valid values', 'error');
        return;
    }

    $.ajax({
        url: `${API_BASE}/cards/withdraw`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ pan, cvv, amount }),
        success: function(data) {
            showMessage('Money withdrawn successfully', 'success');
            $('#withdraw-pan, #withdraw-cvv, #withdraw-amount').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error withdrawing money: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

function depositMoney() {
    const pan = $('#deposit-pan').val();
    const amount = parseFloat($('#deposit-amount').val());

    if (!pan || isNaN(amount) || amount <= 0) {
        showMessage('Please fill all fields with valid values', 'error');
        return;
    }

    $.ajax({
        url: `${API_BASE}/cards/deposit`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ pan, amount }),
        success: function(data) {
            showMessage('Money deposited successfully', 'success');
            $('#deposit-pan, #deposit-amount').val('');
            loadCards();
            loadCardsInfo();
        },
        error: function(xhr) {
            showMessage('Error depositing money: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        }
    });
}

// Cards Info List (format: name surname = pan, cvv)
function loadCardsInfo() {
    $.get(`${API_BASE}/cards`)
        .done(function(data) {
            let html = '<h3>Cards Information:</h3>';
            if (data.length === 0) {
                html += '<p>No cards found</p>';
            } else {
                data.forEach(card => {
                    const name = card.name || '';
                    const surname = card.surname || '';
                    const pan = card.pan || '';
                    const cvv = card.cvv || '';
                    html += `<div class="card-info-item">${name} ${surname} = ${pan}, ${cvv}</div>`;
                });
            }
            $('#cards-info-list').html(html);
        })
        .fail(function(xhr) {
            showMessage('Error loading cards info: ' + (xhr.responseJSON?.error || 'Unknown error'), 'error');
        });
}

// Helper function to generate CVV
const Card = {
    generateCVV: function() {
        return String(Math.floor(Math.random() * 900) + 100);
    }
};

// Helper function to show messages
function showMessage(message, type) {
    const messageDiv = $(`<div class="message ${type}">${message}</div>`);
    $('.container').prepend(messageDiv);
    setTimeout(() => messageDiv.fadeOut(() => messageDiv.remove()), 3000);
}

