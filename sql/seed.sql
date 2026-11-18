-- Z-Store Digital License Key Store - Seed Data (MySQL / MariaDB)
SET NAMES utf8mb4;
USE zstore_jsp;

-- 1. Providers (Categories)
INSERT INTO Provider (ID_Provider, Nama, Thumbnail_Img) VALUES
(1, 'Windows Operating System', '/static/Image/provider_logo/main_windows.webp'),
(2, 'Microsoft Office License', '/static/Image/provider_logo/main_microsoft.webp'),
(3, 'Antivirus License', '/static/Image/provider_logo/main_antivirus.webp'),
(4, 'Tool Software', '/static/Image/provider_logo/main_tool-software.webp'),
(5, 'CorelDraw Software', '/static/Image/provider_logo/main_corelDRAW.webp'),
(6, 'Adobe Software', '/static/Image/provider_logo/main_adobe.webp'),
(7, 'Game', '/static/Image/provider_logo/main_game.webp'),
(8, 'VPN', '/static/Image/provider_logo/main_vpn.webp');

-- 2. Customer Levels
INSERT INTO Level_Pelanggan (ID_Level, Nama_level, Diskon_level, Harga_level) VALUES
(1, 'Standar', 0, 0),
(2, 'Reseller', 10, 150000),
(3, 'VIP', 20, 500000);

-- 3. Sample Users (Demo Admin & Demo Customer)
-- admin@zstore.id / admin123
-- budi@zstore.id  / customer123
INSERT INTO Pelanggan (ID_Pelanggan, ID_Level, Nama_Pelanggan, Gmail_Pelanggan, Password_Hash, Role, Total_Pembelian, Saldo, Status_Akun) VALUES
(1, 3, 'Admin Z-Store', 'admin@zstore.id', 'wHHEjQ1jGbO/wq9+pSEoyQ==:mrIre37npW5Bf8airfZPxcFfRxIX43uDvkcualXPK14=', 'Admin', 0, 5000000, 'Active'),
(2, 2, 'Budi Santoso', 'budi@zstore.id', 'd32a1IAJeqSMFhRyss6Jsw==:WBYpLqnLMVR0BFhgwM7F2OB3W1+2NrL/kXIfGE5In0k=', 'Customer', 1170000, 2500000, 'Active');

-- 4. Products (98 items ported from Website-Toko)
INSERT INTO Produk (ID_Produk, ID_Provider, Nama_Produk, Harga_produk, Deskripsi, Thumbnail_Img) VALUES
-- Windows (1..9)
(1, 1, 'Windows 11 Pro', 1300000, 'Lisensi Windows 11 Pro dengan fitur keamanan dan produktivitas lengkap.', '/static/Image/produk_logo/Windows/Windows 11 Pro.webp'),
(2, 1, 'Windows 10 Pro', 1300000, 'Windows 10 Pro untuk kebutuhan profesional dan bisnis.', '/static/Image/produk_logo/Windows/Windows 10 Pro.webp'),
(3, 1, 'Windows 11 Home', 900000, 'Versi Windows 11 Home untuk pengguna rumahan.', '/static/Image/produk_logo/Windows/Windows 11 Home.webp'),
(4, 1, 'Windows 10 Home', 900000, 'Windows 10 Home dengan fitur dasar untuk penggunaan sehari-hari.', '/static/Image/produk_logo/Windows/Windows 10 Home.webp'),
(5, 1, 'Windows 11 Pro N', 1300000, 'Edisi Windows 11 Pro tanpa komponen multimedia bawaan.', '/static/Image/produk_logo/Windows/Windows 11 Pro N.webp'),
(6, 1, 'Windows 11 Home N', 900000, 'Windows 11 Home versi N tanpa fitur multimedia standar.', '/static/Image/produk_logo/Windows/Windows 11 Home N.webp'),
(7, 1, 'Windows 11 Pro for Workstations', 1500000, 'Edisi Windows 11 untuk workstation dengan performa tinggi.', '/static/Image/produk_logo/Windows/Windows 11 Pro for workstations.webp'),
(8, 1, 'Windows 11 Enterprise', 1500000, 'Windows 11 Enterprise dengan fitur keamanan skala perusahaan.', '/static/Image/produk_logo/Windows/Windows 11 Enterprise.webp'),
(9, 1, 'Windows 11 Education', 1250000, 'Windows 11 Education untuk lingkungan pendidikan.', '/static/Image/produk_logo/Windows/Windows 11 Education.webp'),

-- Microsoft Office (10..27)
(10, 2, 'Microsoft Access 2021', 1100000, 'Aplikasi manajemen database untuk kebutuhan profesional.', '/static/Image/produk_logo/Microsoft/Microsoft Access 2021.webp'),
(11, 2, 'Microsoft Access 2024', 2000000, 'Versi terbaru Microsoft Access dengan peningkatan performa dan kompatibilitas.', '/static/Image/produk_logo/Microsoft/Microsoft Access 2024.webp'),
(12, 2, 'Microsoft Excel 2016', 900000, 'Aplikasi spreadsheet populer untuk pengolahan data.', '/static/Image/produk_logo/Microsoft/Microsoft Excel 2016.webp'),
(13, 2, 'Microsoft Excel 2019', 1200000, 'Microsoft Excel 2019 dengan dukungan fitur analisis modern.', '/static/Image/produk_logo/Microsoft/Microsoft Excel 2019.webp'),
(14, 2, 'Microsoft Excel 2021', 1500000, 'Versi Excel terbaru dengan fungsi rumus lebih lengkap.', '/static/Image/produk_logo/Microsoft/Microsoft Excel 2021.webp'),
(15, 2, 'Microsoft Outlook 2016', 900000, 'Aplikasi email dan kalender untuk bisnis.', '/static/Image/produk_logo/Microsoft/Microsoft Outlook 2016.webp'),
(16, 2, 'Microsoft Outlook 2019', 1250000, 'Microsoft Outlook 2019 dengan peningkatan keamanan email.', '/static/Image/produk_logo/Microsoft/Microsoft Outlook 2019.webp'),
(17, 2, 'Microsoft Outlook 2021', 1500000, 'Versi terbaru Outlook untuk produktivitas email yang lebih baik.', '/static/Image/produk_logo/Microsoft/Microsoft Outlook 2021.webp'),
(18, 2, 'Microsoft Outlook 2024', 2000000, 'Outlook edisi 2024 dengan fitur kolaborasi yang lebih modern.', '/static/Image/produk_logo/Microsoft/Microsoft Outlook 2024.webp'),
(19, 2, 'Microsoft PowerPoint 2024', 2000000, 'Software presentasi profesional dengan fitur desain terbaru.', '/static/Image/produk_logo/Microsoft/Microsoft PowerPoint 2024.webp'),
(20, 2, 'Microsoft Project 2021 Pro', 4000000, 'Software manajemen proyek versi profesional.', '/static/Image/produk_logo/Microsoft/Microsoft Project 2021 Pro.webp'),
(21, 2, 'Microsoft Project 2024 Standard', 4200000, 'Perangkat lunak Project versi Standard untuk kebutuhan manajemen pekerjaan.', '/static/Image/produk_logo/Microsoft/Microsoft Project 2024 Standard.webp'),
(22, 2, 'Microsoft Publisher 2016', 900000, 'Aplikasi desain layout dasar untuk dokumen profesional.', '/static/Image/produk_logo/Microsoft/Microsoft Publisher 2016.webp'),
(23, 2, 'Microsoft Publisher 2019', 1100000, 'Microsoft Publisher 2019 untuk kebutuhan desain dan publikasi.', '/static/Image/produk_logo/Microsoft/Microsoft Publisher 2019.webp'),
(24, 2, 'Microsoft Publisher 2021', 1100000, 'Versi terbaru Publisher dengan dukungan template modern.', '/static/Image/produk_logo/Microsoft/Microsoft Publisher 2021.webp'),
(25, 2, 'Microsoft Word 2016', 900000, 'Aplikasi pengolah kata yang sudah terbukti stabil.', '/static/Image/produk_logo/Microsoft/Microsoft Word 2016.webp'),
(26, 2, 'Microsoft Word 2019', 1250000, 'Microsoft Word 2019 dengan fitur kolaborasi yang lebih baik.', '/static/Image/produk_logo/Microsoft/Microsoft Word 2019.webp'),
(27, 2, 'Microsoft Word 2024', 2000000, 'Word 2024 dengan teknologi AI penunjang penulisan.', '/static/Image/produk_logo/Microsoft/Microsoft Word 2024.webp'),

-- Antivirus (28..36)
(28, 3, 'Avast Premium Security', 500000, 'Perlindungan antivirus lengkap untuk keamanan online dan offline.', '/static/Image/produk_logo/Antivirus/Avast Premium Security.webp'),
(29, 3, 'Avast Ultimate Suite', 700000, 'Paket keamanan premium Avast dengan fitur lengkap antivirus dan VPN.', '/static/Image/produk_logo/Antivirus/Avast Ultimate Suite.webp'),
(30, 3, 'AVG Internet Security', 500000, 'Antivirus populer dengan perlindungan real-time untuk browsing dan email.', '/static/Image/produk_logo/Antivirus/AVG Internet Security.webp'),
(31, 3, 'Bitdefender Total Security', 900000, 'Keamanan komprehensif dengan fitur anti-malware tingkat lanjut.', '/static/Image/produk_logo/Antivirus/Bitdefender Total Security.webp'),
(32, 3, 'ESET NOD32 Antivirus', 400000, 'Antivirus ringan dan cepat dengan perlindungan inti yang solid.', '/static/Image/produk_logo/Antivirus/ESET NOD32 Antivirus.webp'),
(33, 3, 'Kaspersky Plus (Internet Security)', 600000, 'Perlindungan internet modern untuk keamanan saat browsing dan transaksi.', '/static/Image/produk_logo/Antivirus/Kaspersky Plus (Internet Security).webp'),
(34, 3, 'Kaspersky Premium (Total Security)', 1000000, 'Fitur keamanan lengkap termasuk proteksi keluarga dan password manager.', '/static/Image/produk_logo/Antivirus/Kaspersky Premium (Total Security).webp'),
(35, 3, 'Kaspersky Standard (Antivirus)', 600000, 'Versi dasar Kaspersky dengan perlindungan antivirus yang stabil.', '/static/Image/produk_logo/Antivirus/Kaspersky Standard (Antivirus).webp'),
(36, 3, 'McAfee Antivirus Plus', 500000, 'Antivirus terkenal dengan proteksi device tanpa batas.', '/static/Image/produk_logo/Antivirus/McAfee Antivirus Plus.webp'),

-- Tool Software (37..45)
(37, 4, 'Internet Download Manager', 300000, 'Aplikasi download manager cepat dengan fitur percepatan unduhan.', '/static/Image/produk_logo/Tool-Software/Internet Download Manager.webp'),
(38, 4, 'WinRAR', 200000, 'Software kompresi file populer dengan dukungan berbagai format arsip.', '/static/Image/produk_logo/Tool-Software/WinRAR.webp'),
(39, 4, 'Autodesk AutoCAD', 1200000, 'Software desain teknik dan arsitektur paling populer.', '/static/Image/produk_logo/Tool-Software/Autodesk AutoCAD.webp'),
(40, 4, 'Autodesk Revit', 1200000, 'Software BIM untuk arsitek dan engineer dengan workflow profesional.', '/static/Image/produk_logo/Tool-Software/Autodesk Revit.webp'),
(41, 4, 'EaseUS Data Recovery Wizard Professional', 800000, 'Aplikasi recovery data profesional untuk memulihkan file yang hilang.', '/static/Image/produk_logo/Tool-Software/EaseUS Data Recovery Wizard Professional.webp'),
(42, 4, 'Power BI Pro', 800000, 'Software visualisasi data untuk kebutuhan analisis bisnis.', '/static/Image/produk_logo/Tool-Software/Power BI Pro.webp'),
(43, 4, 'VMware vSphere 7.0 Enterprise Plus', 1500000, 'Platform virtualisasi kelas enterprise dengan fitur lengkap.', '/static/Image/produk_logo/Tool-Software/VMware vSphere 7.0 Enterprise Plus.webp'),
(44, 4, 'VMware Workstation 17 Player', 900000, 'Software virtualisasi ringan untuk menjalankan mesin virtual.', '/static/Image/produk_logo/Tool-Software/VMware Workstation 17 Player.webp'),
(45, 4, 'VMware Workstation 17 Pro', 1200000, 'Software virtualisasi profesional dengan fitur lanjutan.', '/static/Image/produk_logo/Tool-Software/VMware Workstation 17 Pro.webp'),

-- CorelDraw (46..55)
(46, 5, 'CorelDRAW Graphics Suite 2023 - Lifetime', 1800000, 'Paket desain lengkap untuk kebutuhan profesional grafis dan ilustrasi.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2023.webp'),
(47, 5, 'CorelDRAW Graphics Suite 2022 - Lifetime', 1800000, 'Software desain vektor dan layout dengan fitur modern dan stabil.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2022.webp'),
(48, 5, 'CorelDRAW Graphics Suite 2021 - Lifetime', 1800000, 'Versi 2021 dengan fitur kolaborasi dan editing vektor canggih.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2021.webp'),
(49, 5, 'CorelDRAW Graphics Suite 2020 - Lifetime', 1600000, 'Paket desain grafis untuk ilustrasi, layout, dan manipulasi foto.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2020.webp'),
(50, 5, 'CorelDRAW Graphics Suite 2019 - Lifetime', 1600000, 'Software desain grafis fleksibel untuk kreator profesional.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2019.webp'),
(51, 5, 'CorelDRAW Graphics Suite 2017 - Lifetime', 1400000, 'Versi stabil CorelDRAW untuk kebutuhan desain dan ilustrasi.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Graphics Suite 2017.webp'),
(52, 5, 'CorelDRAW Standard 2021 - Lifetime', 1300000, 'Versi Standard untuk pengguna rumahan dan usaha kecil.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Standard 2021.webp'),
(53, 5, 'CorelDRAW Standard 2020 - Lifetime', 1300000, 'Paket desain dasar yang ringan dan mudah digunakan.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Standard 2020.webp'),
(54, 5, 'CorelDRAW Technical Suite 2021 - Lifetime', 2000000, 'Software ilustrasi teknis untuk engineering dan manufaktur.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Technical Suite 2021.webp'),
(55, 5, 'CorelDRAW Technical Suite 2020 - Lifetime', 2000000, 'Paket teknis untuk membuat skema, diagram, dan ilustrasi industri.', '/static/Image/produk_logo/CorelDRAW/CorelDRAW Technical Suite 2020.webp'),

-- Adobe (56..72)
(56, 6, 'Adobe Photoshop Elements 2023', 800000, 'Software editing foto ringan untuk kreator dan pengguna rumahan.', '/static/Image/produk_logo/Adobe/Adobe Photoshop Elements 2023.webp'),
(57, 6, 'Adobe Photoshop Elements 2025', 900000, 'Versi terbaru Photoshop Elements dengan fitur AI modern.', '/static/Image/produk_logo/Adobe/Adobe Photoshop Elements 2025.webp'),
(58, 6, 'Adobe Premiere Elements 2025', 900000, 'Software editing video mudah digunakan untuk kebutuhan pribadi.', '/static/Image/produk_logo/Adobe/Adobe Premiere Elements 2025.webp'),
(59, 6, 'Adobe Premiere Elements 2024', 850000, 'Premiere Elements 2024 dengan fitur otomatisasi cerdas.', '/static/Image/produk_logo/Adobe/Adobe Premiere Elements 2024.webp'),
(60, 6, 'Adobe Premiere Elements 2023', 800000, 'Software editing video ringan dan intuitif.', '/static/Image/produk_logo/Adobe/Adobe Premiere Elements 2023.webp'),
(61, 6, 'Adobe Premiere Pro', 1200000, 'Software editing video profesional standar industri.', '/static/Image/produk_logo/Adobe/Adobe Premiere Pro.webp'),
(62, 6, 'Adobe Creative Cloud - 1 Bulan (Global)', 150000, 'Akses seluruh aplikasi Adobe Creative Cloud selama 1 bulan.', '/static/Image/produk_logo/Adobe/Adobe Creative Cloud 1 Bulan.webp'),
(63, 6, 'Adobe Creative Cloud - 3 Bulan (Global)', 350000, 'Langganan Adobe Creative Cloud selama 3 bulan.', '/static/Image/produk_logo/Adobe/Adobe Creative Cloud 3 Bulan.webp'),
(64, 6, 'Adobe Creative Cloud - 1 Tahun (Global)', 1200000, 'Akses penuh aplikasi Adobe selama 1 tahun.', '/static/Image/produk_logo/Adobe/Adobe Creative Cloud 1 Tahun.webp'),
(65, 6, 'Adobe Acrobat Pro 2018', 600000, 'Software PDF profesional dengan fitur lengkap.', '/static/Image/produk_logo/Adobe/Adobe Acrobat Pro 2018.webp'),
(66, 6, 'Adobe Acrobat Pro 2017', 500000, 'Versi Acrobat Pro 2017 untuk kebutuhan dokumen PDF.', '/static/Image/produk_logo/Adobe/Adobe Acrobat Pro 2017.webp'),
(67, 6, 'Adobe Acrobat Pro 2020', 700000, 'Versi Acrobat modern dengan peningkatan performa.', '/static/Image/produk_logo/Adobe/Adobe Acrobat Pro 2020.webp'),
(68, 6, 'Adobe Acrobat Standard 2018', 500000, 'Versi Standard Acrobat untuk kebutuhan PDF dasar.', '/static/Image/produk_logo/Adobe/Adobe Acrobat Standard 2018.webp'),
(69, 6, 'Adobe Acrobat Standard 2020', 550000, 'Acrobat Standard 2020 dengan fitur PDF terbaru.', '/static/Image/produk_logo/Adobe/Adobe Acrobat Standard 2020.webp'),
(70, 6, 'Adobe Substance 3D Painter - 1 Month', 400000, 'Software texturing 3D profesional untuk desainer.', '/static/Image/produk_logo/Adobe/Adobe Substance 3D Painter.webp'),
(71, 6, 'Adobe Substance 3D Painter - 3 Month', 900000, 'Langganan Substance 3D Painter selama 3 bulan.', '/static/Image/produk_logo/Adobe/Adobe Substance 3D Painter(2).webp'),
(72, 6, 'Adobe AI Assistant for Acrobat - 1 Year', 800000, 'Fitur AI untuk mempercepat pekerjaan dokumen di Acrobat.', '/static/Image/produk_logo/Adobe/Adobe AI Assistant for Acrobat.webp'),

-- Games (73..84)
(73, 7, 'Cyberpunk 2077 Ultimate Edition - GOG', 400000, 'Edisi lengkap Cyberpunk 2077 termasuk semua DLC.', '/static/Image/produk_logo/Game/Cyberpunk 2077 Ultimate Edition – GOG.webp'),
(74, 7, 'God of War - Steam', 500000, 'Game aksi-petualangan epik dengan grafis sinematik.', '/static/Image/produk_logo/Game/God of War – Steam.webp'),
(75, 7, 'Grand Theft Auto V - Steam', 250000, 'Game open world populer dengan mode story dan GTA Online.', '/static/Image/produk_logo/Game/Grand Theft Auto V – Steam.webp'),
(76, 7, 'Hogwarts Legacy - Steam', 400000, 'RPG dunia sihir dengan eksplorasi Hogwarts yang memukau.', '/static/Image/produk_logo/Game/Hogwarts Legacy – Steam.webp'),
(77, 7, 'Marvels Spider-Man Remastered - Steam', 450000, 'Game superhero dengan aksi cepat dan cerita menarik.', '/static/Image/produk_logo/Game/Marvel’s Spider-Man Remastered – Steam.webp'),
(78, 7, 'Minecraft Bedrock Edition - Launcher', 350000, 'Versi Bedrock Minecraft dengan dukungan cross-platform.', '/static/Image/produk_logo/Game/Minecraft Bedrock Edition – Launcher.webp'),
(79, 7, 'Minecraft Java Edition - Launcher', 350000, 'Versi Java resmi Minecraft dengan akses penuh ke server komunitas.', '/static/Image/produk_logo/Game/Minecraft Java Edition – Launcher.webp'),
(80, 7, 'No Mans Sky - Steam', 300000, 'Game eksplorasi luar angkasa dengan dunia prosedural tanpa batas.', '/static/Image/produk_logo/Game/No Man’s Sky – Steam.webp'),
(81, 7, 'PLAYERUNKNOWNS BATTLEGROUNDS (PUBG) - Steam', 300000, 'Game battle royale populer dengan aksi kompetitif.', '/static/Image/produk_logo/Game/PLAYERUNKNOWN’S BATTLEGROUNDS (PUBG) – Steam.webp'),
(82, 7, 'Red Dead Redemption 2 - Rockstar Games', 350000, 'Game aksi-petualangan dengan dunia terbuka dan cerita mendalam.', '/static/Image/produk_logo/Game/Red Dead Redemption 2 – Rockstar Games.webp'),
(83, 7, 'Red Dead Redemption 2 Ultimate Edition - Rockstar Games', 450000, 'Edisi Ultimate dengan konten tambahan eksklusif.', '/static/Image/produk_logo/Game/Red Dead Redemption 2 Ultimate Edition – Rockstar Games.webp'),
(84, 7, 'The Forest - Steam', 250000, 'Game survival horror dengan elemen crafting dan eksplorasi.', '/static/Image/produk_logo/Game/The Forest – Steam.webp'),

-- VPN (85..98)
(85, 8, 'CyberGhost 3 Device - 2 Year', 420000, 'VPN cepat dengan perlindungan untuk 3 perangkat selama 2 tahun.', '/static/Image/produk_logo/VPN/Cyber Ghost 3 Device – 2 Year.webp'),
(86, 8, 'CyberGhost 5 Device - 5 Year', 700000, 'Paket VPN CyberGhost untuk 5 perangkat selama 5 tahun.', '/static/Image/produk_logo/VPN/Cyber Ghost 5 Device – 5 Year.webp'),
(87, 8, 'ExpressVPN - 1 Month', 66500, 'VPN premium dengan server cepat dan aman untuk penggunaan 1 bulan.', '/static/Image/produk_logo/VPN/Express VPN – 1 Month.webp'),
(88, 8, 'ExpressVPN - 3 Month', 175000, 'Langganan ExpressVPN selama 3 bulan.', '/static/Image/produk_logo/VPN/Express VPN – 3 Month.webp'),
(89, 8, 'ExpressVPN - 1 Year', 472500, 'Paket tahunan ExpressVPN dengan performa tinggi.', '/static/Image/produk_logo/VPN/Express VPN – 1 Year.webp'),
(90, 8, 'NordVPN - 1 Month', 52500, 'VPN cepat dengan enkripsi kuat untuk penggunaan 1 bulan.', '/static/Image/produk_logo/VPN/NordVPN – 1 Month.webp'),
(91, 8, 'NordVPN - 3 Month', 157500, 'Langganan NordVPN selama 3 bulan.', '/static/Image/produk_logo/VPN/NordVPN – 3 Month.webp'),
(92, 8, 'NordVPN - 1 Year', 315000, 'Paket 1 tahun NordVPN dengan perlindungan maksimal.', '/static/Image/produk_logo/VPN/NordVPN – 1 Year.webp'),
(93, 8, 'ProtonVPN - 1 Month', 49000, 'VPN fokus privasi dari ProtonMail untuk penggunaan 1 bulan.', '/static/Image/produk_logo/VPN/ProtonVPN – 1 Month.webp'),
(94, 8, 'ProtonVPN - 3 Month', 147000, 'Langganan ProtonVPN selama 3 bulan.', '/static/Image/produk_logo/VPN/ProtonVPN – 3 Month.webp'),
(95, 8, 'ProtonVPN - 1 Year', 297500, 'Paket tahunan ProtonVPN dengan fitur privasi tinggi.', '/static/Image/produk_logo/VPN/ProtonVPN – 1 Year.webp'),
(96, 8, 'Surfshark VPN - 1 Month', 42000, 'VPN unlimited device dengan harga ekonomis.', '/static/Image/produk_logo/VPN/Surfshark VPN – 1 Month.webp'),
(97, 8, 'Surfshark VPN - 3 Month', 126000, 'Langganan Surfshark VPN selama 3 bulan.', '/static/Image/produk_logo/VPN/Surfshark VPN – 3 Month.webp'),
(98, 8, 'Surfshark VPN - 1 Year', 245000, 'Paket tahunan Surfshark dengan fitur lengkap dan unlimited device.', '/static/Image/produk_logo/VPN/Surfshark VPN – 1 Year.webp');

-- 5. Tags
INSERT INTO Tags (ID_Tag, Nama_Tag) VALUES
(1, 'Operating System'),
(2, 'Productivity'),
(3, 'Security'),
(4, 'Utility'),
(5, 'Design'),
(6, 'Creative'),
(7, 'Gaming'),
(8, 'Open World'),
(9, 'FPS'),
(10, 'Survival');

-- 6. Product_Tags (Mapped accurately to IDs 1..98)
INSERT INTO Product_Tags (ID_Produk, ID_Tag) VALUES
(1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),(9,1),
(10,2),(11,2),(12,2),(13,2),(14,2),(15,2),(16,2),(17,2),(18,2),(19,2),(20,2),(21,2),(22,2),(23,2),(24,2),(25,2),(26,2),(27,2),
(28,3),(29,3),(30,3),(31,3),(32,3),(33,3),(34,3),(35,3),(36,3),
(37,4),(38,4),(39,5),(40,5),(41,4),(42,2),(43,4),(44,4),(45,4),
(46,5),(47,5),(48,5),(49,5),(50,5),(51,5),(52,5),(53,5),(54,5),(55,5),
(56,6),(57,6),(58,6),(59,6),(60,6),(61,6),(62,6),(63,6),(64,6),(65,2),(66,2),(67,2),(68,2),(69,2),(70,6),(71,6),(72,2),
(73,7),(74,7),(75,7),(76,7),(77,7),(78,7),(79,7),(80,7),(81,7),(82,7),(83,7),(84,7),
(73,8),(75,8),(82,8),(83,8),(81,9),(84,10),
(85,3),(86,3),(87,3),(88,3),(89,3),(90,3),(91,3),(92,3),(93,3),(94,3),(95,3),(96,3),(97,3),(98,3);

-- 7. Seed License Stocks (3 available keys for every product + sample sold keys for demo transactions)
INSERT INTO Stocks (ID_Produk, kode_lisensi, status)
SELECT ID_Produk, CONCAT('ZKEY-', LPAD(ID_Produk, 3, '0'), '-A1B2-C3D4'), 'Tersedia' FROM Produk;

INSERT INTO Stocks (ID_Produk, kode_lisensi, status)
SELECT ID_Produk, CONCAT('ZKEY-', LPAD(ID_Produk, 3, '0'), '-E5F6-G7H8'), 'Tersedia' FROM Produk;

INSERT INTO Stocks (ID_Produk, kode_lisensi, status)
SELECT ID_Produk, CONCAT('ZKEY-', LPAD(ID_Produk, 3, '0'), '-J9K0-L1M2'), 'Tersedia' FROM Produk;

-- Sample sold license stocks for initial demonstration transactions
INSERT INTO Stocks (ID_Stk, ID_Produk, kode_lisensi, claimedAt, status) VALUES
(1001, 1, 'BNB5-JCVT-FS1I-0IYP', CURDATE(), 'Terjual'),
(1002, 73, 'G02H-ASTX-AREB-CS59', CURDATE(), 'Terjual');

-- 8. Sample Invoices & Transactions for Customer #2 (budi@zstore.id)
INSERT INTO Invoice (ID_Invoice, Provider, Provider_Payment_ID, Payment_Link, External_ID, Fee, Net_Amount, Paid_At, Expires_At, Status) VALUES
(1, 'SALDO', NULL, 'invoice?ext=INV-DEMO0001', 'INV-DEMO0001', 0, 1170000, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Paid'),
(2, 'LOCAL', NULL, 'invoice?ext=INV-DEMO0002', 'INV-DEMO0002', 0, 360000, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), 'Pending');

INSERT INTO Transaksi (ID_Trx, ID_Pelanggan, ID_Invoice, Total_Pembayaran, Metode_Pembayaran, Status, Gmail_trx, Nama_Penerima) VALUES
(1, 2, 1, 1170000, 'SALDO', 'Completed', 'budi@zstore.id', 'Budi Santoso'),
(2, 2, 2, 360000, 'QRIS', 'Unpaid', 'budi@zstore.id', 'Budi Santoso');

INSERT INTO Detail_Transaksi (ID_Trx, ID_Stk, Harga_beli) VALUES
(1, 1001, 1170000),
(2, 1002, 360000);

