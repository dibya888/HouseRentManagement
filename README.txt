HouseRentManagement v2.0.0
=============================

Secure Multi-User House/Flat/Tenant Rent Management System

Version: 2.0.0
Developer/Vendor: Dibya Jyoti Dhar
Installer: HouseRentManagement-2.0.0.exe

------------------------------------------------------------
IMPORTANT UPGRADE NOTICE FROM v1.0
------------------------------------------------------------

HouseRentManagement v2.0 introduces a new secure multi-user encrypted database system.

If you are upgrading from v1.0:

1. Uninstall HouseRentManagement v1.0 from Windows Settings / Apps.
2. Do NOT delete AppData.
3. Install HouseRentManagement v2.0.
4. Login as Admin.
5. Existing v1.0 data will be migrated after Admin login.
6. After confirming your data, create a new backup from Settings.

Do NOT delete this folder during upgrade:

C:\Users\<username>\AppData\Roaming\HouseRentManagement

Your old v1.0 database, if present, is expected here:

C:\Users\<username>\AppData\Roaming\HouseRentManagement\database\rent.db

After migration, v2.0 stores user databases here:

C:\Users\<username>\AppData\Roaming\HouseRentManagement\users\{userId}\rent.db

Authentication/security data is stored here:

C:\Users\<username>\AppData\Roaming\HouseRentManagement\auth\auth.db

------------------------------------------------------------
DEFAULT LOGIN AFTER FRESH INSTALL
------------------------------------------------------------

Username: admin
Password: 1234

Please set recovery PIN and generate emergency recovery keys after first login.

------------------------------------------------------------
WHAT'S NEW IN v2.0
------------------------------------------------------------

- Secure multi-user support
- Separate encrypted rent database for each user
- Admin-controlled user creation, enable, disable, and delete
- Auth/security database separated from rent/business database
- Password-based database key unlocking
- Recovery PIN password reset with data preserved
- Emergency key password reset with data preserved
- Portable backup and restore using .hrmbak files
- Same-user restore protection to prevent cross-user data restore
- Factory reset support
- Legacy v1.0 data migration into Admin account

------------------------------------------------------------
BACKUP AND RESTORE
------------------------------------------------------------

Use Settings -> Backup Database to create a portable backup file.

New backup format:

.hrmbak

Important:

Old raw .db backups are NOT portable in the new encrypted multi-user system.
After upgrading to v2.0, create a new backup from Settings.

Portable .hrmbak backup behavior:

- admin backup can restore to admin
- testuser backup can restore to testuser
- admin backup cannot restore to testuser
- testuser backup cannot restore to admin

After Factory Reset, restore using a .hrmbak backup created from v2.0.

------------------------------------------------------------
PASSWORD RECOVERY
------------------------------------------------------------

Recovery PIN:

- Resets password
- Preserves encrypted database data

Emergency Recovery Keys:

- One-time emergency keys
- Reset password
- Preserve encrypted database data if generated in v2.0 after the secure recovery update

Important:

After upgrading to v2.0, generate new emergency recovery keys from Settings.
Older keys may not support encrypted database recovery.

------------------------------------------------------------
FACTORY RESET
------------------------------------------------------------

Factory Reset removes all users and app data and recreates a fresh Admin account.

Default login after Factory Reset:

Username: admin
Password: 1234

If you need to restore data after Factory Reset, use a v2.0 .hrmbak backup.

------------------------------------------------------------
INSTALLATION NOTES
------------------------------------------------------------

The Windows installer includes:

- Application JAR
- Runtime dependency JARs
- JavaFX libraries
- SQLite encrypted JDBC driver
- PDFBox
- Apache POI
- XMLBeans
- Commons libraries
- App icon
- License file

Installer options used:

- License agreement
- Install directory chooser
- Start Menu entry
- Desktop shortcut
- Fixed upgrade UUID for future versions

Fixed upgrade UUID:

6f2a37f8-6e9f-4c1e-a2f7-9f18f7c6b431

------------------------------------------------------------
KNOWN UPGRADE BEHAVIOR
------------------------------------------------------------

v1.0 may install side-by-side with v2.0 because v1.0 used a different installer identity.

Recommended upgrade path:

1. Uninstall v1.0.
2. Do NOT delete AppData.
3. Install v2.0.
4. Login as Admin.
5. Confirm migration success.
6. Create a new .hrmbak backup.

Future versions after v2.0 should use the same upgrade UUID and should upgrade more cleanly.

------------------------------------------------------------
SUPPORT / TROUBLESHOOTING
------------------------------------------------------------

If the app does not open after installation:

1. Make sure the latest v2.0 installer was used.
2. Uninstall older/broken versions.
3. Reinstall v2.0.
4. Do not delete AppData unless intentionally performing a full reset.

If migration does not happen:

- Confirm old database exists at:
  C:\Users\<username>\AppData\Roaming\HouseRentManagement\database\rent.db

If restore fails:

- Confirm you are restoring a .hrmbak backup for the same username.
- Cross-user restore is blocked by design.

------------------------------------------------------------
LICENSE
------------------------------------------------------------

See LICENSE.txt included with this release.