db.users.update({_id: ObjectId('5d347d63376b144f69c8465d')}, {$set: {lastName: 'Wijarn'}});

db.messages.update({user: ObjectId('5d347d63376b144f69c8465d')}, {$set: {name: 'Nattha Wijarn'}});