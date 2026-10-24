const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');
const { test, before, after, beforeEach } = require('node:test');
const fs = require('node:fs');

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || 'demo-no-project';
const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8085'
).split(':');
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserProfile(userId, now) {
  return {
    userId,
    username: 'KakarotMain',
    level: 3,
    exp: 120,
    maxExp: 400,
    gold: 8500,
    gems: 40,
    selectedCharacterId: 'kakarot_z',
    rankedRating: 240,
    rankTier: 'Silver',
    wins: 6,
    losses: 2,
    unlockedIds: ['kakarot_z', 'kamado_sun'],
    upgradesPacked: 'kakarot_z:2,1,1',
    createdAt: now,
    updatedAt: now,
  };
}

function validLeaderboard(userId, now) {
  return {
    userId,
    username: 'KakarotMain',
    characterId: 'kakarot_z',
    rating: 240,
    tier: 'Silver',
    wins: 6,
    losses: 2,
    isPublic: true,
    createdAt: now,
    updatedAt: now,
  };
}

function validMatchRoom(hostUid, now) {
  return {
    roomCode: 'AB12CD',
    mode: 'ranked',
    status: 'waiting',
    hostUid,
    hostName: 'HostFighter',
    hostCharId: 'kakarot_z',
    hostRating: 300,
    hostHpLvl: 1,
    hostPwrLvl: 1,
    hostCdLvl: 0,
    guestUid: '',
    guestName: '',
    guestCharId: '',
    guestRating: 0,
    guestHpLvl: 0,
    guestPwrLvl: 0,
    guestCdLvl: 0,
    seed: 123456,
    hostCmdSeq: 0,
    hostDirX: 0,
    hostDirY: 0,
    hostUltSeq: 0,
    hostSyncHp: 405,
    guestCmdSeq: 0,
    guestDirX: 0,
    guestDirY: 0,
    guestUltSeq: 0,
    guestSyncHp: 360,
    winnerUid: '',
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync('./firestore.rules', 'utf8'),
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test('unauthenticated clients cannot read or write any collection', async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection('users').doc('alice').get());
  await assertFails(unauthDb.collection('users').doc('alice').set(validUserProfile('alice', now)));
  await assertFails(unauthDb.collection('leaderboard').doc('alice').get());
  await assertFails(unauthDb.collection('match_rooms').doc('room_AB12CD').get());
});

test('users profile allows owner CRUD, blocks cross-user reads/writes, shadow fields, and createdAt mutation', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const malloryDb = testEnv.authenticatedContext('mallory').firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(aliceDb.collection('users').doc('alice').set(validUserProfile('alice', now)));
  await assertSucceeds(aliceDb.collection('users').doc('alice').get());

  await assertFails(malloryDb.collection('users').doc('alice').get());
  await assertFails(malloryDb.collection('users').doc('alice').update({ gold: 999999, updatedAt: now }));

  // Shadow field injection rejected
  await assertFails(
    aliceDb.collection('users').doc('alice').update({ isAdmin: true, updatedAt: now })
  );

  const later = new Date(Date.now() - 500);
  await assertSucceeds(
    aliceDb.collection('users').doc('alice').update({ gold: 12000, wins: 7, updatedAt: later })
  );

  await assertFails(
    aliceDb.collection('users').doc('alice').update({ createdAt: later, updatedAt: later })
  );
  await assertFails(
    aliceDb.collection('users').doc('alice').update({
      username: 'ThisUsernameIsWayTooLongForTwentyFourChars',
      updatedAt: later,
    })
  );
});

test('leaderboard enforces query constraint on list and owner-only writes', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const now = new Date(Date.now() - 1500);

  await assertSucceeds(aliceDb.collection('leaderboard').doc('alice').set(validLeaderboard('alice', now)));

  await assertSucceeds(bobDb.collection('leaderboard').where('isPublic', '==', true).get());
  await assertFails(bobDb.collection('leaderboard').get());
  await assertFails(bobDb.collection('leaderboard').doc('alice').update({ rating: 0, updatedAt: now }));
});

test('match_rooms enforces host creation, guest join transition, action-scoped updates, and terminal lock', async () => {
  const hostDb = testEnv.authenticatedContext('host_1').firestore();
  const guestDb = testEnv.authenticatedContext('guest_2').firestore();
  const intruderDb = testEnv.authenticatedContext('intruder_3').firestore();
  const now = new Date(Date.now() - 2000);

  const roomRefHost = hostDb.collection('match_rooms').doc('room_AB12CD');
  await assertSucceeds(roomRefHost.set(validMatchRoom('host_1', now)));

  await assertSucceeds(guestDb.collection('match_rooms').where('status', '==', 'waiting').get());
  await assertFails(guestDb.collection('match_rooms').get());

  const joinTime = new Date(Date.now() - 1000);
  // Host cannot join their own room
  await assertFails(
    roomRefHost.update({
      status: 'active',
      guestUid: 'host_1',
      guestName: 'SelfJoin',
      guestCharId: 'kamado_sun',
      guestRating: 200,
      guestHpLvl: 0,
      guestPwrLvl: 0,
      guestCdLvl: 0,
      guestSyncHp: 350,
      updatedAt: joinTime,
    })
  );

  const roomRefGuest = guestDb.collection('match_rooms').doc('room_AB12CD');
  await assertSucceeds(
    roomRefGuest.update({
      status: 'active',
      guestUid: 'guest_2',
      guestName: 'RivalGuest',
      guestCharId: 'kamado_sun',
      guestRating: 280,
      guestHpLvl: 1,
      guestPwrLvl: 0,
      guestCdLvl: 0,
      guestSyncHp: 385,
      updatedAt: joinTime,
    })
  );

  const roomRefIntruder = intruderDb.collection('match_rooms').doc('room_AB12CD');
  await assertFails(
    roomRefIntruder.update({
      guestCmdSeq: 1,
      guestDirX: 0.5,
      guestDirY: -0.8,
      updatedAt: joinTime,
    })
  );

  await assertFails(
    roomRefGuest.update({
      hostCmdSeq: 5,
      updatedAt: joinTime,
    })
  );

  const combatTime = new Date(Date.now() - 500);
  await assertSucceeds(
    roomRefHost.update({
      hostCmdSeq: 1,
      hostDirX: 0.45,
      hostDirY: -0.89,
      updatedAt: combatTime,
    })
  );
  await assertSucceeds(
    roomRefGuest.update({
      guestCmdSeq: 1,
      guestDirX: -0.45,
      guestDirY: 0.89,
      updatedAt: combatTime,
    })
  );

  await assertSucceeds(
    roomRefHost.update({
      status: 'finished',
      guestSyncHp: 0,
      winnerUid: 'host_1',
      updatedAt: combatTime,
    })
  );

  // Terminal state lock: finished match cannot be reopened or mutated
  await assertFails(
    roomRefHost.update({
      status: 'active',
      winnerUid: '',
      updatedAt: combatTime,
    })
  );

  await assertSucceeds(roomRefHost.delete());
});
