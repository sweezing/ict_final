const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { Pool } = require('pg');
const { MongoClient } = require('mongodb');

const app = express();
const PORT = 3000;
const path = require('path');

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Serve static files from public directory
app.use(express.static(path.join(__dirname, 'public')));

// Database configuration
let dbType = 'postgres'; // 'postgres' or 'mongo'
const POSTGRES_CONFIG = {
    host: 'localhost',
    port: 5432,
    database: 'banking_system',
    user: 'postgres',
    password: ''
};

const MONGO_CONFIG = {
    url: 'mongodb+srv://mangodbswiz:123@cluster0.guocwkk.mongodb.net/?appName=Cluster0',
    dbName: 'banking_system'
};

let mongoClient = null;
let mongoDb = null;

// PostgreSQL connection pool
const pgPool = new Pool(POSTGRES_CONFIG);

// MongoDB connection
async function connectMongo() {
    if (!mongoClient) {
        mongoClient = new MongoClient(MONGO_CONFIG.url);
        await mongoClient.connect();
        mongoDb = mongoClient.db(MONGO_CONFIG.dbName);
    }
    return mongoDb;
}

// Switch database endpoint
app.post('/api/switch-db', (req, res) => {
    const { dbType: newDbType } = req.body;
    if (newDbType === 'postgres' || newDbType === 'mongo') {
        dbType = newDbType;
        res.json({ success: true, message: `Switched to ${newDbType}` });
    } else {
        res.status(400).json({ success: false, message: 'Invalid database type' });
    }
});

app.get('/api/db-type', (req, res) => {
    res.json({ dbType });
});

// ========== CARD USERS ENDPOINTS ==========

// GET /card-users - Get all card users
app.get('/api/card-users', async (req, res) => {
    try {
        if (dbType === 'postgres') {
            const result = await pgPool.query('SELECT * FROM card_users');
            res.json(result.rows);
        } else {
            const db = await connectMongo();
            const users = await db.collection('card_users').find({}).toArray();
            res.json(users);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /card-users/:iin - Get card user by IIN
app.get('/api/card-users/:iin', async (req, res) => {
    try {
        const { iin } = req.params;
        if (dbType === 'postgres') {
            const result = await pgPool.query('SELECT * FROM card_users WHERE iin = $1', [iin]);
            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            res.json(result.rows[0]);
        } else {
            const db = await connectMongo();
            const user = await db.collection('card_users').findOne({ iin });
            if (!user) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            res.json(user);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /card-users - Create card user
app.post('/api/card-users', async (req, res) => {
    try {
        const { name, surname, iin } = req.body;
        if (dbType === 'postgres') {
            const result = await pgPool.query(
                'INSERT INTO card_users (name, surname, iin) VALUES ($1, $2, $3) RETURNING *',
                [name, surname, iin]
            );
            res.status(201).json(result.rows[0]);
        } else {
            const db = await connectMongo();
            const user = { name, surname, iin };
            await db.collection('card_users').insertOne(user);
            res.status(201).json(user);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// PUT /card-users/:iin - Update card user
app.put('/api/card-users/:iin', async (req, res) => {
    try {
        const { iin } = req.params;
        const { name, surname } = req.body;
        if (dbType === 'postgres') {
            const result = await pgPool.query(
                'UPDATE card_users SET name = $1, surname = $2 WHERE iin = $3 RETURNING *',
                [name, surname, iin]
            );
            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            res.json(result.rows[0]);
        } else {
            const db = await connectMongo();
            const result = await db.collection('card_users').updateOne(
                { iin },
                { $set: { name, surname } }
            );
            if (result.matchedCount === 0) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            const user = await db.collection('card_users').findOne({ iin });
            res.json(user);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// DELETE /card-users/:iin - Delete card user
app.delete('/api/card-users/:iin', async (req, res) => {
    try {
        const { iin } = req.params;
        if (dbType === 'postgres') {
            const result = await pgPool.query('DELETE FROM card_users WHERE iin = $1', [iin]);
            if (result.rowCount === 0) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            res.json({ success: true });
        } else {
            const db = await connectMongo();
            const result = await db.collection('card_users').deleteOne({ iin });
            if (result.deletedCount === 0) {
                return res.status(404).json({ error: 'Card user not found' });
            }
            res.json({ success: true });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ========== CARDS ENDPOINTS ==========

// GET /cards - Get all cards
app.get('/api/cards', async (req, res) => {
    try {
        if (dbType === 'postgres') {
            const result = await pgPool.query('SELECT * FROM cards ORDER BY card_id');
            res.json(result.rows);
        } else {
            const db = await connectMongo();
            const cards = await db.collection('cards').find({}).toArray();
            res.json(cards);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /cards/:id - Get card by ID (PAN for MongoDB)
app.get('/api/cards/:id', async (req, res) => {
    try {
        const { id } = req.params;
        if (dbType === 'postgres') {
            const result = await pgPool.query('SELECT * FROM cards WHERE card_id = $1', [id]);
            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json(result.rows[0]);
        } else {
            const db = await connectMongo();
            const card = await db.collection('cards').findOne({ pan: id });
            if (!card) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json(card);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /cards - Create card
app.post('/api/cards', async (req, res) => {
    try {
        const { pan, cvv, dateOfExpire, iin, currency, balance } = req.body;

        if (!iin) {
            return res.status(400).json({ error: 'IIN is required' });
        }

        if (dbType === 'postgres') {
            const userResult = await pgPool.query(
                'SELECT name, surname FROM card_users WHERE iin = $1',
                [iin]
            );

            if (userResult.rows.length === 0) {
                return res.status(404).json({ error: 'Card user with this IIN not found' });
            }

            const { name, surname } = userResult.rows[0];

            const result = await pgPool.query(
                'INSERT INTO cards (pan, cvv, date_of_expire, name, surname, currency, balance) VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *',
                [pan, cvv, dateOfExpire, name, surname, currency, balance]
            );
            res.status(201).json(result.rows[0]);
        } else {
            const db = await connectMongo();

            const user = await db.collection('card_users').findOne({ iin });
            if (!user) {
                return res.status(404).json({ error: 'Card user with this IIN not found' });
            }

            const card = {
                pan,
                cvv,
                dateOfExpire,
                name: user.name,
                surname: user.surname,
                currency,
                balance
            };

            await db.collection('cards').insertOne(card);
            res.status(201).json(card);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.put('/api/cards/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { pan, cvv, dateOfExpire, name, surname, currency, balance } = req.body;

        const updates = {};
        if (pan !== undefined) updates.pan = pan;
        if (cvv !== undefined) updates.cvv = cvv;
        if (dateOfExpire !== undefined) updates.date_of_expire = dateOfExpire;
        if (name !== undefined) updates.name = name;
        if (surname !== undefined) updates.surname = surname;
        if (currency !== undefined) updates.currency = currency;
        if (balance !== undefined) updates.balance = balance;

        if (Object.keys(updates).length === 0) {
            return res.status(400).json({ error: 'No fields to update' });
        }

        if (dbType === 'postgres') {
            const setClauses = [];
            const values = [];
            let index = 1;

            for (const [column, value] of Object.entries(updates)) {
                setClauses.push(`${column} = $${index}`);
                values.push(value);
                index += 1;
            }

            const query = `UPDATE cards SET ${setClauses.join(', ')} WHERE card_id = $${index} RETURNING *`;
            values.push(id);

            const result = await pgPool.query(query, values);
            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json(result.rows[0]);
        } else {
            const db = await connectMongo();
            const result = await db.collection('cards').updateOne(
                { pan: id },
                { $set: {
                    ...(pan !== undefined ? { pan } : {}),
                    ...(cvv !== undefined ? { cvv } : {}),
                    ...(dateOfExpire !== undefined ? { dateOfExpire } : {}),
                    ...(name !== undefined ? { name } : {}),
                    ...(surname !== undefined ? { surname } : {}),
                    ...(currency !== undefined ? { currency } : {}),
                    ...(balance !== undefined ? { balance } : {})
                } }
            );
            if (result.matchedCount === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            const card = await db.collection('cards').findOne({ pan: pan || id });
            res.json(card);
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// DELETE /cards/:id - Delete card
app.delete('/api/cards/:id', async (req, res) => {
    try {
        const { id } = req.params;
        if (dbType === 'postgres') {
            const result = await pgPool.query('DELETE FROM cards WHERE card_id = $1', [id]);
            if (result.rowCount === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json({ success: true });
        } else {
            const db = await connectMongo();
            const result = await db.collection('cards').deleteOne({ pan: id });
            if (result.deletedCount === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json({ success: true });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /cards/transfer - Transfer money
app.post('/api/cards/transfer', async (req, res) => {
    try {
        const { fromPan, toPan, amount } = req.body;
        if (dbType === 'postgres') {
            const client = await pgPool.connect();
            try {
                await client.query('BEGIN');
                const fromResult = await client.query(
                    'UPDATE cards SET balance = balance - $1 WHERE pan = $2 AND balance >= $1 RETURNING *',
                    [amount, fromPan]
                );
                if (fromResult.rows.length === 0) {
                    await client.query('ROLLBACK');
                    return res.status(400).json({ error: 'Insufficient balance or card not found' });
                }
                const toResult = await client.query(
                    'UPDATE cards SET balance = balance + $1 WHERE pan = $2 RETURNING *',
                    [amount, toPan]
                );
                if (toResult.rows.length === 0) {
                    await client.query('ROLLBACK');
                    return res.status(404).json({ error: 'Destination card not found' });
                }
                await client.query('COMMIT');
                res.json({ success: true, fromCard: fromResult.rows[0], toCard: toResult.rows[0] });
            } catch (error) {
                await client.query('ROLLBACK');
                throw error;
            } finally {
                client.release();
            }
        } else {
            const db = await connectMongo();
            const fromCard = await db.collection('cards').findOne({ pan: fromPan });
            const toCard = await db.collection('cards').findOne({ pan: toPan });
            if (!fromCard || !toCard || fromCard.balance < amount) {
                return res.status(400).json({ error: 'Transfer failed' });
            }
            await db.collection('cards').updateOne(
                { pan: fromPan },
                { $inc: { balance: -amount } }
            );
            await db.collection('cards').updateOne(
                { pan: toPan },
                { $inc: { balance: amount } }
            );
            res.json({ success: true });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /cards/withdraw - Withdraw money (requires CVV)
app.post('/api/cards/withdraw', async (req, res) => {
    try {
        const { pan, cvv, amount } = req.body;
        if (dbType === 'postgres') {
            const result = await pgPool.query(
                'UPDATE cards SET balance = balance - $1 WHERE pan = $2 AND cvv = $3 AND balance >= $1 RETURNING *',
                [amount, pan, cvv]
            );
            if (result.rows.length === 0) {
                return res.status(400).json({ error: 'Invalid CVV, insufficient balance, or card not found' });
            }
            res.json({ success: true, card: result.rows[0] });
        } else {
            const db = await connectMongo();
            const result = await db.collection('cards').updateOne(
                { pan, cvv, balance: { $gte: amount } },
                { $inc: { balance: -amount } }
            );
            if (result.modifiedCount === 0) {
                return res.status(400).json({ error: 'Invalid CVV, insufficient balance, or card not found' });
            }
            const card = await db.collection('cards').findOne({ pan });
            res.json({ success: true, card });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /cards/deposit - Deposit money
app.post('/api/cards/deposit', async (req, res) => {
    try {
        const { pan, amount } = req.body;
        if (dbType === 'postgres') {
            const result = await pgPool.query(
                'UPDATE cards SET balance = balance + $1 WHERE pan = $2 RETURNING *',
                [amount, pan]
            );
            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            res.json({ success: true, card: result.rows[0] });
        } else {
            const db = await connectMongo();
            const result = await db.collection('cards').updateOne(
                { pan },
                { $inc: { balance: amount } }
            );
            if (result.matchedCount === 0) {
                return res.status(404).json({ error: 'Card not found' });
            }
            const card = await db.collection('cards').findOne({ pan });
            res.json({ success: true, card });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
    console.log(`Current database: ${dbType}`);
});

