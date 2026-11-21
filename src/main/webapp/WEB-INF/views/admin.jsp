<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
    <div>
      <h2 class="fw-bold text-white mb-1">Admin Panel Z-Store</h2>
      <p class="text-muted-soft mb-0">Kelola kategori, produk, stok lisensi, tag, pelanggan, dan transaksi.</p>
    </div>
    <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost">Lihat Etalase Toko</a>
  </div>

  <c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success"><c:out value="${sessionScope.flashSuccess}" /></div>
    <c:remove var="flashSuccess" scope="session" />
  </c:if>
  <c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger"><c:out value="${sessionScope.flashError}" /></div>
    <c:remove var="flashError" scope="session" />
  </c:if>

  <!-- Ringkasan Statistik -->
  <div class="row g-3 mb-4">
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Kategori</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${providers.size()}" /></div>
      </div>
    </div>
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Produk</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${products.size()}" /></div>
      </div>
    </div>
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Stok Lisensi</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${stocks.size()}" /></div>
      </div>
    </div>
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Tag</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${tags.size()}" /></div>
      </div>
    </div>
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Pelanggan</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${customers.size()}" /></div>
      </div>
    </div>
    <div class="col-6 col-md-2">
      <div class="glass-card p-3 text-center">
        <div class="small text-muted-soft">Transaksi</div>
        <div class="fs-4 fw-bold text-white"><c:out value="${transactions.size()}" /></div>
      </div>
    </div>
  </div>

  <!-- Tab Navigation -->
  <div class="pills d-flex gap-2 flex-wrap mb-4">
    <a class="pill ${activeTab == 'products' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=products">Produk</a>
    <a class="pill ${activeTab == 'providers' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=providers">Kategori / Provider</a>
    <a class="pill ${activeTab == 'stocks' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=stocks">Stok Lisensi</a>
    <a class="pill ${activeTab == 'tags' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=tags">Tags</a>
    <a class="pill ${activeTab == 'customers' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=customers">Pelanggan</a>
    <a class="pill ${activeTab == 'transactions' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin?tab=transactions">Transaksi</a>
  </div>

  <!-- TAB 1: PRODUK -->
  <c:if test="${activeTab == 'products'}">
    <div class="row g-4">
      <div class="col-lg-4">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">
            <c:choose>
              <c:when test="${editingProduct != null}">Edit Produk #<c:out value="${editingProduct.idProduk}" /></c:when>
              <c:otherwise>Tambah Produk Baru</c:otherwise>
            </c:choose>
          </h5>
          <form method="POST" action="${pageContext.request.contextPath}/admin" enctype="multipart/form-data">
            <input type="hidden" name="tab" value="products">
            <input type="hidden" name="action" value="${editingProduct != null ? 'product_update' : 'product_create'}">
            <c:if test="${editingProduct != null}">
              <input type="hidden" name="id_produk" value="${editingProduct.idProduk}">
            </c:if>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Provider / Kategori</label>
              <select name="id_provider" class="form-select form-dark" required>
                <c:forEach var="pr" items="${providers}">
                  <option value="${pr.idProvider}" ${editingProduct != null && editingProduct.idProvider == pr.idProvider ? 'selected' : ''}>
                    <c:out value="${pr.nama}" />
                  </option>
                </c:forEach>
              </select>
            </div>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Nama Produk</label>
              <input type="text" name="nama_produk" class="form-control form-dark"
                     value="<c:out value='${editingProduct != null ? editingProduct.namaProduk : ""}'/>" required>
            </div>

            <div class="row g-2 mb-2">
              <div class="col-6">
                <label class="form-label small fw-semibold">Harga (Rp)</label>
                <input type="number" name="harga_produk" class="form-control form-dark" min="1000"
                       value="${editingProduct != null ? editingProduct.hargaProduk : 100000}" required>
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Status</label>
                <select name="status_produk" class="form-select form-dark">
                  <option value="Available" ${editingProduct != null && editingProduct.statusProduk == 'Available' ? 'selected' : ''}>Available</option>
                  <option value="Unavailable" ${editingProduct != null && editingProduct.statusProduk == 'Unavailable' ? 'selected' : ''}>Unavailable</option>
                </select>
              </div>
            </div>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Jenis Produk</label>
              <input type="text" name="jenis_produk" class="form-control form-dark"
                     value="<c:out value='${editingProduct != null ? editingProduct.jenisProduk : "Item"}'/>">
            </div>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Image</label>
              <c:set var="prodHasThumb" value="${editingProduct != null && editingProduct.hasCustomThumbnail}" />
              <div class="img-upload-box" data-img-upload>
                <input type="file" name="thumbnail_file" class="img-upload-input" accept="image/png,image/jpeg,image/webp,image/gif">
                <input type="hidden" name="remove_thumbnail" value="0" class="img-upload-remove-flag">
                <div class="img-upload-trigger" role="button" tabindex="0" title="Pilih gambar produk">
                  <span class="img-upload-icon">
                    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <rect x="3" y="4" width="18" height="16" rx="2.5"/>
                      <circle cx="8.5" cy="9.5" r="1.6"/>
                      <path d="M21 16l-5.2-5.2a1.4 1.4 0 00-2 0L6 18.5"/>
                    </svg>
                  </span>
                  <span class="img-upload-name ${prodHasThumb ? '' : 'is-placeholder'}"
                        data-placeholder="Pilih gambar produk (PNG/JPG/WEBP)...">
                    <c:choose>
                      <c:when test="${prodHasThumb}"><c:out value="${editingProduct.thumbnailFileName}" /></c:when>
                      <c:otherwise>Pilih gambar produk (PNG/JPG/WEBP)...</c:otherwise>
                    </c:choose>
                  </span>
                </div>
                <button type="button" class="img-upload-clear ${prodHasThumb ? '' : 'is-hidden'}"
                        title="Hapus gambar" aria-label="Hapus gambar">
                  <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    <path d="M10 11v6M14 11v6"/>
                    <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Deskripsi</label>
              <textarea name="deskripsi" rows="3" class="form-control form-dark"><c:out value="${editingProduct != null ? editingProduct.deskripsi : ''}" /></textarea>
            </div>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Tags (pisahkan dengan koma)</label>
              <div class="tag-input-box" data-tag-editor>
                <input type="hidden" name="tags_csv" class="tag-hidden-csv"
                       value="<c:out value='${editingProduct != null ? editingProduct.tagsCsv : ""}'/>">
                <input type="text" class="tag-chip-field" placeholder="Ketik tag lalu koma atau Enter...">
              </div>
              <c:if test="${not empty tags}">
                <div class="tag-suggest-list" data-tag-suggestions>
                  <c:forEach var="t" items="${tags}">
                    <button type="button" class="tag-suggest-btn" data-tag-name="<c:out value='${t.namaTag}'/>">
                      + <c:out value="${t.namaTag}" />
                    </button>
                  </c:forEach>
                </div>
              </c:if>
            </div>

            <div class="d-flex gap-2">
              <button type="submit" class="btn btn-accent flex-grow-1">
                ${editingProduct != null ? 'Simpan Perubahan' : 'Tambah Produk'}
              </button>
              <c:if test="${editingProduct != null}">
                <a href="${pageContext.request.contextPath}/admin?tab=products" class="btn btn-ghost">Batal</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">Daftar Produk (<c:out value="${products.size()}" />)</h5>
          <div class="admin-table-wrap">
            <table class="table table-dark-custom table-hover align-middle small">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Produk</th>
                  <th>Kategori</th>
                  <th>Harga</th>
                  <th>Stok</th>
                  <th>Status</th>
                  <th class="text-end">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="p" items="${products}">
                  <tr>
                    <td>#<c:out value="${p.idProduk}" /></td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <img src="<c:out value='${p.thumbnailSrc(pageContext.request.contextPath)}'/>"
                             alt="" class="admin-tbl-thumb"
                             onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
                        <div>
                          <div class="fw-semibold"><c:out value="${p.namaProduk}" /></div>
                          <c:if test="${not empty p.tags}">
                            <div class="p-tags-row" style="margin-top:2px;">
                              <c:forEach var="pt" items="${p.tags}">
                                <span class="p-tag-mini"><c:out value="${pt.namaTag}" /></span>
                              </c:forEach>
                            </div>
                          </c:if>
                        </div>
                      </div>
                    </td>
                    <td><c:out value="${p.providerNama}" /></td>
                    <td>Rp <fmt:formatNumber value="${p.hargaProduk}" groupingUsed="true" /></td>
                    <td><c:out value="${p.availableStock}" /></td>
                    <td><span class="badge text-bg-dark border border-secondary"><c:out value="${p.statusProduk}" /></span></td>
                    <td class="text-end text-nowrap">
                      <a href="${pageContext.request.contextPath}/admin?tab=products&editProduct=${p.idProduk}"
                         class="btn btn-sm btn-ghost">Edit</a>
                      <a href="${pageContext.request.contextPath}/admin?tab=stocks&filterProduct=${p.idProduk}"
                         class="btn btn-sm btn-ghost">Stok</a>
                      <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                            onsubmit="return confirm('Hapus produk ini?');">
                        <input type="hidden" name="tab" value="products">
                        <input type="hidden" name="action" value="product_delete">
                        <input type="hidden" name="id_produk" value="${p.idProduk}">
                        <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </c:if>

  <!-- TAB 2: PROVIDERS -->
  <c:if test="${activeTab == 'providers'}">
    <div class="row g-4">
      <div class="col-lg-4">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">
            ${editingProvider != null ? 'Edit Kategori / Provider' : 'Tambah Kategori Baru'}
          </h5>
          <form method="POST" action="${pageContext.request.contextPath}/admin" enctype="multipart/form-data">
            <input type="hidden" name="tab" value="providers">
            <input type="hidden" name="action" value="${editingProvider != null ? 'provider_update' : 'provider_create'}">
            <c:if test="${editingProvider != null}">
              <input type="hidden" name="id_provider" value="${editingProvider.idProvider}">
            </c:if>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Nama Kategori / Provider</label>
              <input type="text" name="nama" class="form-control form-dark"
                     value="<c:out value='${editingProvider != null ? editingProvider.nama : ""}'/>" required>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-semibold">Image</label>
              <c:set var="provHasThumb" value="${editingProvider != null && editingProvider.hasCustomThumbnail}" />
              <div class="img-upload-box" data-img-upload>
                <input type="file" name="thumbnail_file" class="img-upload-input" accept="image/png,image/jpeg,image/webp,image/gif">
                <input type="hidden" name="remove_thumbnail" value="0" class="img-upload-remove-flag">
                <div class="img-upload-trigger" role="button" tabindex="0" title="Pilih gambar kategori">
                  <span class="img-upload-icon">
                    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <rect x="3" y="4" width="18" height="16" rx="2.5"/>
                      <circle cx="8.5" cy="9.5" r="1.6"/>
                      <path d="M21 16l-5.2-5.2a1.4 1.4 0 00-2 0L6 18.5"/>
                    </svg>
                  </span>
                  <span class="img-upload-name ${provHasThumb ? '' : 'is-placeholder'}"
                        data-placeholder="Pilih gambar (PNG/JPG/WEBP)...">
                    <c:choose>
                      <c:when test="${provHasThumb}"><c:out value="${editingProvider.thumbnailFileName}" /></c:when>
                      <c:otherwise>Pilih gambar (PNG/JPG/WEBP)...</c:otherwise>
                    </c:choose>
                  </span>
                </div>
                <button type="button" class="img-upload-clear ${provHasThumb ? '' : 'is-hidden'}"
                        title="Hapus gambar" aria-label="Hapus gambar">
                  <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    <path d="M10 11v6M14 11v6"/>
                    <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="d-flex gap-2">
              <button type="submit" class="btn btn-accent flex-grow-1">
                ${editingProvider != null ? 'Simpan Perubahan' : 'Tambah Kategori'}
              </button>
              <c:if test="${editingProvider != null}">
                <a href="${pageContext.request.contextPath}/admin?tab=providers" class="btn btn-ghost">Batal</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">Daftar Kategori / Provider</h5>
          <div class="admin-table-wrap">
            <table class="table table-dark-custom table-hover align-middle small">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nama</th>
                  <th>Jumlah Produk</th>
                  <th class="text-end">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="pr" items="${providers}">
                  <tr>
                    <td>#<c:out value="${pr.idProvider}" /></td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <img src="<c:out value='${pr.thumbnailSrc(pageContext.request.contextPath)}'/>"
                             alt="" class="admin-tbl-thumb"
                             onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
                        <span class="fw-semibold"><c:out value="${pr.nama}" /></span>
                      </div>
                    </td>
                    <td><c:out value="${pr.productCount}" /></td>
                    <td class="text-end text-nowrap">
                      <a href="${pageContext.request.contextPath}/admin?tab=providers&editProvider=${pr.idProvider}"
                         class="btn btn-sm btn-ghost">Edit</a>
                      <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                            onsubmit="return confirm('Hapus provider ini?');">
                        <input type="hidden" name="tab" value="providers">
                        <input type="hidden" name="action" value="provider_delete">
                        <input type="hidden" name="id_provider" value="${pr.idProvider}">
                        <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </c:if>

  <!-- TAB 3: STOCKS -->
  <c:if test="${activeTab == 'stocks'}">
    <div class="row g-4">
      <div class="col-lg-4">
        <c:choose>
          <c:when test="${editingStock != null}">
            <div class="glass-card p-4">
              <h5 class="fw-bold text-white mb-3">Edit Stok #<c:out value="${editingStock.idStk}" /></h5>
              <form method="POST" action="${pageContext.request.contextPath}/admin">
                <input type="hidden" name="tab" value="stocks">
                <input type="hidden" name="action" value="stock_update">
                <input type="hidden" name="id_stk" value="${editingStock.idStk}">

                <div class="mb-2">
                  <label class="form-label small fw-semibold">Produk</label>
                  <select name="id_produk" class="form-select form-dark">
                    <c:forEach var="p" items="${products}">
                      <option value="${p.idProduk}" ${editingStock.idProduk == p.idProduk ? 'selected' : ''}>
                        <c:out value="${p.namaProduk}" />
                      </option>
                    </c:forEach>
                  </select>
                </div>
                <div class="mb-2">
                  <label class="form-label small fw-semibold">Kode Lisensi</label>
                  <input type="text" name="kode_lisensi" class="form-control form-dark"
                         value="<c:out value='${editingStock.kodeLisensi}'/>" required>
                </div>
                <div class="mb-2">
                  <label class="form-label small fw-semibold">Status</label>
                  <select name="status" class="form-select form-dark">
                    <option value="Tersedia" ${editingStock.status == 'Tersedia' ? 'selected' : ''}>Tersedia</option>
                    <option value="Terjual" ${editingStock.status == 'Terjual' ? 'selected' : ''}>Terjual</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-semibold">Expired At (Opsional)</label>
                  <input type="date" name="expired_at" class="form-control form-dark" value="${editingStock.expiredAt}">
                </div>
                <div class="d-flex gap-2">
                  <button type="submit" class="btn btn-accent flex-grow-1">Simpan</button>
                  <a href="${pageContext.request.contextPath}/admin?tab=stocks" class="btn btn-ghost">Batal</a>
                </div>
              </form>
            </div>
          </c:when>
          <c:otherwise>
            <div class="glass-card p-4 mb-3">
              <h5 class="fw-bold text-white mb-3">Tambah Stok Lisensi (Batch)</h5>
              <form method="POST" action="${pageContext.request.contextPath}/admin">
                <input type="hidden" name="tab" value="stocks">
                <input type="hidden" name="action" value="stock_batch">
                <div class="mb-2">
                  <label class="form-label small fw-semibold">Produk</label>
                  <select name="id_produk" class="form-select form-dark" required>
                    <c:forEach var="p" items="${products}">
                      <option value="${p.idProduk}" ${filterProduct == p.idProduk ? 'selected' : ''}>
                        <c:out value="${p.namaProduk}" />
                      </option>
                    </c:forEach>
                  </select>
                </div>
                <div class="mb-2">
                  <label class="form-label small fw-semibold">Daftar Kode Lisensi (1 baris per kode)</label>
                  <textarea name="kode_lisensi_batch" rows="4" class="form-control form-dark"
                            placeholder="XXXX-YYYY-ZZZZ-1111&#10;XXXX-YYYY-ZZZZ-2222" required></textarea>
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-semibold">Tanggal Kedaluwarsa (Opsional)</label>
                  <input type="date" name="expired_at" class="form-control form-dark">
                </div>
                <button type="submit" class="btn btn-accent w-100">Tambahkan Stok</button>
              </form>
            </div>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="col-lg-8">
        <div class="glass-card p-4">
          <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
            <h5 class="fw-bold text-white mb-0">Daftar Stok Lisensi</h5>
            <form method="GET" action="${pageContext.request.contextPath}/admin" class="d-flex gap-2">
              <input type="hidden" name="tab" value="stocks">
              <select name="filterProduct" class="form-select form-select-sm form-dark" onchange="this.form.submit()">
                <option value="0">Semua Produk</option>
                <c:forEach var="p" items="${products}">
                  <option value="${p.idProduk}" ${filterProduct == p.idProduk ? 'selected' : ''}>
                    <c:out value="${p.namaProduk}" />
                  </option>
                </c:forEach>
              </select>
            </form>
          </div>
          <div class="admin-table-wrap">
            <table class="table table-dark-custom table-hover align-middle small">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Produk</th>
                  <th>Kode Lisensi</th>
                  <th>Status</th>
                  <th class="text-end">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="s" items="${stocks}">
                  <tr>
                    <td>#<c:out value="${s.idStk}" /></td>
                    <td><c:out value="${s.namaProduk}" /></td>
                    <td><code><c:out value="${s.kodeLisensi}" /></code></td>
                    <td>
                      <span class="status-badge ${s.status == 'Tersedia' ? 'status-completed' : 'status-pending'}">
                        <c:out value="${s.status}" />
                      </span>
                    </td>
                    <td class="text-end text-nowrap">
                      <a href="${pageContext.request.contextPath}/admin?tab=stocks&editStock=${s.idStk}"
                         class="btn btn-sm btn-ghost">Edit</a>
                      <c:if test="${s.status == 'Tersedia'}">
                        <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                              onsubmit="return confirm('Hapus stok lisensi ini?');">
                          <input type="hidden" name="tab" value="stocks">
                          <input type="hidden" name="action" value="stock_delete">
                          <input type="hidden" name="id_stk" value="${s.idStk}">
                          <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                        </form>
                      </c:if>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </c:if>

  <!-- TAB 4: TAGS -->
  <c:if test="${activeTab == 'tags'}">
    <div class="row g-4">
      <div class="col-lg-4">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">
            <c:choose>
              <c:when test="${editingTag != null}">Edit Tag #<c:out value="${editingTag.idTag}" /></c:when>
              <c:otherwise>Tambah Tag Baru</c:otherwise>
            </c:choose>
          </h5>
          <form method="POST" action="${pageContext.request.contextPath}/admin">
            <input type="hidden" name="tab" value="tags">
            <input type="hidden" name="action" value="${editingTag != null ? 'tag_update' : 'tag_create'}">
            <c:if test="${editingTag != null}">
              <input type="hidden" name="id_tag" value="${editingTag.idTag}">
            </c:if>
            <div class="mb-3">
              <label class="form-label small fw-semibold">
                ${editingTag != null ? 'Nama Tag' : 'Nama Tag (bisa dipisah koma)'}
              </label>
              <input type="text" name="nama_tag" class="form-control form-dark"
                     value="<c:out value='${editingTag != null ? editingTag.namaTag : ""}'/>"
                     placeholder="${editingTag != null ? 'Nama tag baru' : 'Misal: Enterprise, Lifetime, Promo'}" required>
            </div>
            <div class="d-flex gap-2">
              <button type="submit" class="btn btn-accent flex-grow-1">
                ${editingTag != null ? 'Simpan Perubahan' : 'Tambah Tag'}
              </button>
              <c:if test="${editingTag != null}">
                <a href="${pageContext.request.contextPath}/admin?tab=tags" class="btn btn-ghost">Batal</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">Daftar Tag</h5>
          <div class="admin-table-wrap">
            <table class="table table-dark-custom table-hover align-middle small">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nama Tag</th>
                  <th>Dipakai Produk</th>
                  <th class="text-end">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${tags}">
                  <tr>
                    <td>#<c:out value="${t.idTag}" /></td>
                    <td class="fw-semibold"><c:out value="${t.namaTag}" /></td>
                    <td><c:out value="${t.productCount}" /></td>
                    <td class="text-end text-nowrap">
                      <a href="${pageContext.request.contextPath}/admin?tab=tags&editTag=${t.idTag}"
                         class="btn btn-sm btn-ghost">Edit</a>
                      <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                            onsubmit="return confirm('Hapus tag ini?');">
                        <input type="hidden" name="tab" value="tags">
                        <input type="hidden" name="action" value="tag_delete">
                        <input type="hidden" name="id_tag" value="${t.idTag}">
                        <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </c:if>

  <!-- TAB 5: PELANGGAN -->
  <c:if test="${activeTab == 'customers'}">
    <div class="glass-card p-4">
      <h5 class="fw-bold text-white mb-3">Kelola Pelanggan &amp; Level</h5>
      <div class="admin-table-wrap">
        <table class="table table-dark-custom table-hover align-middle small">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nama &amp; Email</th>
              <th>Role</th>
              <th>Level</th>
              <th>Saldo (Rp)</th>
              <th>Status</th>
              <th class="text-end">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="c" items="${customers}">
              <tr>
                <td>#<c:out value="${c.idPelanggan}" /></td>
                <td>
                  <div class="fw-semibold"><c:out value="${c.namaPelanggan}" /></div>
                  <div class="text-muted-soft"><c:out value="${c.gmailPelanggan}" /></div>
                </td>
                <td><span class="badge text-bg-dark border border-secondary"><c:out value="${c.role}" /></span></td>
                <td colspan="3">
                  <form method="POST" action="${pageContext.request.contextPath}/admin" class="row g-2 align-items-center">
                    <input type="hidden" name="tab" value="customers">
                    <input type="hidden" name="action" value="customer_update">
                    <input type="hidden" name="id_pelanggan" value="${c.idPelanggan}">
                    <div class="col-md-4">
                      <select name="id_level" class="form-select form-select-sm form-dark">
                        <c:forEach var="lv" items="${levels}">
                          <option value="${lv.idLevel}" ${c.idLevel == lv.idLevel ? 'selected' : ''}>
                            <c:out value="${lv.namaLevel}" /> (<c:out value="${lv.diskonLevel}" />%)
                          </option>
                        </c:forEach>
                      </select>
                    </div>
                    <div class="col-md-4">
                      <input type="number" name="saldo" class="form-control form-control-sm form-dark" value="${c.saldo}" min="0">
                    </div>
                    <div class="col-md-2">
                      <select name="status_akun" class="form-select form-select-sm form-dark">
                        <option value="Active" ${c.statusAkun == 'Active' ? 'selected' : ''}>Active</option>
                        <option value="Suspended" ${c.statusAkun == 'Suspended' ? 'selected' : ''}>Suspended</option>
                      </select>
                    </div>
                    <div class="col-md-2">
                      <button type="submit" class="btn btn-sm btn-dash-primary w-100">Simpan</button>
                    </div>
                  </form>
                </td>
                <td class="text-end">
                  <c:if test="${!c.admin}">
                    <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                          onsubmit="return confirm('Hapus pelanggan ini?');">
                      <input type="hidden" name="tab" value="customers">
                      <input type="hidden" name="action" value="customer_delete">
                      <input type="hidden" name="id_pelanggan" value="${c.idPelanggan}">
                      <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                    </form>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </c:if>

  <!-- TAB 6: TRANSAKSI -->
  <c:if test="${activeTab == 'transactions'}">
    <div class="glass-card p-4">
      <h5 class="fw-bold text-white mb-3">Kelola Transaksi &amp; Invoice</h5>
      <div class="admin-table-wrap">
        <table class="table table-dark-custom table-hover align-middle small">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Pembeli</th>
              <th>Total</th>
              <th>Metode</th>
              <th>Status</th>
              <th class="text-end">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${transactions}">
              <c:set var="adminOrderId" value="${t.invoice != null && not empty t.invoice.externalId ? t.invoice.externalId : '#'.concat(t.idTrx)}" />
              <tr>
                <td>
                  <div class="inv-order-id fw-semibold text-white"><c:out value="${adminOrderId}" /></div>
                  <div class="text-muted-soft" style="font-size:0.72rem;">Trx #<c:out value="${t.idTrx}" /></div>
                </td>
                <td>
                  <div class="fw-semibold"><c:out value="${not empty t.namaPenerima ? t.namaPenerima : (not empty t.namaPelanggan ? t.namaPelanggan : 'Guest')}" /></div>
                  <div class="text-muted-soft"><c:out value="${t.gmailTrx}" /></div>
                </td>
                <td>Rp <fmt:formatNumber value="${t.totalPembayaran}" groupingUsed="true" /></td>
                <td><c:out value="${t.metodePembayaran}" /></td>
                <td>
                  <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-flex gap-2 align-items-center">
                    <input type="hidden" name="tab" value="transactions">
                    <input type="hidden" name="action" value="trx_update">
                    <input type="hidden" name="id_trx" value="${t.idTrx}">
                    <select name="status" class="form-select form-select-sm form-dark" style="width:136px;">
                      <option value="Unpaid" ${t.status == 'Unpaid' ? 'selected' : ''}>Unpaid</option>
                      <option value="Completed" ${t.status == 'Completed' ? 'selected' : ''}>Completed</option>
                      <option value="Failed" ${t.status == 'Failed' ? 'selected' : ''}>Failed</option>
                      <option value="Expired" ${t.status == 'Expired' ? 'selected' : ''}>Expired</option>
                      <option value="Cancelled" ${t.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button type="submit" class="btn btn-sm btn-dash-primary">Ubah</button>
                  </form>
                </td>
                <td class="text-end text-nowrap">
                  <a href="${pageContext.request.contextPath}/invoice?order_id=<c:out value='${t.invoice != null && not empty t.invoice.externalId ? t.invoice.externalId : ""}'/>"
                     class="btn btn-sm btn-ghost">Invoice</a>
                  <form method="POST" action="${pageContext.request.contextPath}/admin" class="d-inline"
                        onsubmit="return confirm('Hapus transaksi ini?');">
                    <input type="hidden" name="tab" value="transactions">
                    <input type="hidden" name="action" value="trx_delete">
                    <input type="hidden" name="id_trx" value="${t.idTrx}">
                    <button type="submit" class="btn btn-sm btn-outline-danger">Hapus</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </c:if>
</div>

<script>
/**
 * AdminCustomSelect — portaled fixed-position dark dropdown so menus inside
 * scrollable tables (.admin-table-wrap) are never clipped or unreachable.
 */
(function () {
  'use strict';

  var SEARCH_THRESHOLD = 10;
  var CHEVRON = '<svg class="csd-chevron" viewBox="0 0 16 16" fill="none" '
    + 'stroke="currentColor" stroke-width="2" stroke-linecap="round" '
    + 'stroke-linejoin="round" aria-hidden="true"><polyline points="4 6 8 10 12 6"/></svg>';

  var activeState = null;

  function init() {
    document.querySelectorAll('select.form-dark, select.form-select-sm.form-dark')
      .forEach(function (sel) {
        buildCustomSelect(sel);
      });

    document.addEventListener('click', function (e) {
      if (!activeState) return;
      if (!activeState.wrap.contains(e.target) && !activeState.menu.contains(e.target)) {
        closeActive();
      }
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && activeState) {
        closeActive();
      }
    });

    window.addEventListener('resize', closeActive);
    window.addEventListener('scroll', closeActive, true);
  }

  function buildCustomSelect(sel) {
    var wrap = document.createElement('div');
    wrap.className = 'csd-wrap';
    if (sel.classList.contains('form-select-sm')) {
      wrap.classList.add('csd-sm');
    }
    if (sel.style.width) {
      wrap.style.width = sel.style.width;
    }

    sel.parentNode.insertBefore(wrap, sel);
    wrap.appendChild(sel);

    var trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.className = 'csd-trigger';
    trigger.setAttribute('aria-haspopup', 'listbox');
    trigger.setAttribute('aria-expanded', 'false');

    var label = document.createElement('span');
    label.className = 'csd-trigger-label';
    trigger.appendChild(label);
    trigger.insertAdjacentHTML('beforeend', CHEVRON);
    wrap.appendChild(trigger);

    var menu = document.createElement('div');
    menu.className = 'csd-menu csd-portal';
    menu.setAttribute('role', 'listbox');

    var searchInput = null;
    if (sel.options.length > SEARCH_THRESHOLD) {
      var searchWrap = document.createElement('div');
      searchWrap.className = 'csd-search-wrap';
      searchInput = document.createElement('input');
      searchInput.type = 'text';
      searchInput.className = 'csd-search';
      searchInput.placeholder = 'Cari...';
      searchInput.setAttribute('aria-label', 'Cari pilihan');
      searchWrap.appendChild(searchInput);
      menu.appendChild(searchWrap);
    }

    var list = document.createElement('div');
    list.className = 'csd-list';
    menu.appendChild(list);

    function buildOptions() {
      list.innerHTML = '';
      Array.prototype.forEach.call(sel.options, function (opt) {
        var item = document.createElement('div');
        item.className = 'csd-opt' + (opt.selected ? ' is-selected' : '');
        item.textContent = opt.text;
        item.dataset.value = opt.value;
        item.setAttribute('role', 'option');
        item.setAttribute('aria-selected', opt.selected ? 'true' : 'false');

        item.addEventListener('click', function (e) {
          e.stopPropagation();
          selectOption(sel, label, list, item);
        });

        list.appendChild(item);
      });
    }

    function syncLabel() {
      var selected = sel.options[sel.selectedIndex];
      label.textContent = selected ? selected.text : '';
    }

    trigger.addEventListener('click', function (e) {
      e.stopPropagation();
      if (activeState && activeState.wrap === wrap) {
        closeActive();
      } else {
        closeActive();
        openMenu(wrap, trigger, menu, searchInput, list);
      }
    });

    if (searchInput) {
      searchInput.addEventListener('input', function () {
        var q = searchInput.value.toLowerCase();
        list.querySelectorAll('.csd-opt').forEach(function (item) {
          item.classList.toggle('is-hidden',
            q.length > 0 && item.textContent.toLowerCase().indexOf(q) === -1);
        });
      });
      searchInput.addEventListener('click', function (e) { e.stopPropagation(); });
    }

    sel.addEventListener('change', function () {
      syncLabel();
      list.querySelectorAll('.csd-opt').forEach(function (item) {
        var isSel = item.dataset.value === sel.value;
        item.classList.toggle('is-selected', isSel);
        item.setAttribute('aria-selected', isSel ? 'true' : 'false');
      });
    });

    var mo = new MutationObserver(function () {
      buildOptions();
      syncLabel();
    });
    mo.observe(sel, { childList: true });

    buildOptions();
    syncLabel();
  }

  function positionMenu(trigger, menu) {
    var rect = trigger.getBoundingClientRect();
    var width = Math.max(rect.width, 144);
    menu.style.width = width + 'px';

    var left = rect.left;
    if (left + width > window.innerWidth - 12) {
      left = Math.max(12, window.innerWidth - width - 12);
    }
    menu.style.left = left + 'px';

    var menuHeight = menu.offsetHeight || 200;
    var spaceBelow = window.innerHeight - rect.bottom - 12;
    var spaceAbove = rect.top - 12;
    if (spaceBelow < menuHeight && spaceAbove > spaceBelow) {
      menu.style.top = Math.max(12, rect.top - menuHeight - 6) + 'px';
    } else {
      menu.style.top = (rect.bottom + 6) + 'px';
    }
  }

  function openMenu(wrap, trigger, menu, searchInput, list) {
    document.body.appendChild(menu);
    menu.classList.add('is-open');
    trigger.classList.add('is-open');
    trigger.setAttribute('aria-expanded', 'true');
    activeState = { wrap: wrap, trigger: trigger, menu: menu };

    if (searchInput) {
      searchInput.value = '';
      list.querySelectorAll('.csd-opt').forEach(function (item) {
        item.classList.remove('is-hidden');
      });
    }

    positionMenu(trigger, menu);

    if (searchInput) {
      setTimeout(function () { searchInput.focus(); }, 20);
    }

    var selected = list.querySelector('.csd-opt.is-selected');
    if (selected) {
      selected.scrollIntoView({ block: 'nearest' });
    }
  }

  function closeActive() {
    if (!activeState) return;
    activeState.menu.classList.remove('is-open');
    if (activeState.menu.parentNode === document.body) {
      document.body.removeChild(activeState.menu);
    }
    activeState.trigger.classList.remove('is-open');
    activeState.trigger.setAttribute('aria-expanded', 'false');
    activeState = null;
  }

  function selectOption(sel, label, list, item) {
    sel.value = item.dataset.value;
    var selectedOpt = sel.options[sel.selectedIndex];
    label.textContent = selectedOpt ? selectedOpt.text : item.textContent;

    list.querySelectorAll('.csd-opt').forEach(function (opt) {
      var isSel = opt === item;
      opt.classList.toggle('is-selected', isSel);
      opt.setAttribute('aria-selected', isSel ? 'true' : 'false');
    });

    closeActive();
    sel.dispatchEvent(new Event('change', { bubbles: true }));
  }

  /* Reusable Image Upload Box */
  function initImageUploads() {
    document.querySelectorAll('[data-img-upload]').forEach(function (box) {
      var input = box.querySelector('.img-upload-input');
      var trigger = box.querySelector('.img-upload-trigger');
      var nameEl = box.querySelector('.img-upload-name');
      var clearBtn = box.querySelector('.img-upload-clear');
      var removeFlag = box.querySelector('.img-upload-remove-flag');
      var placeholder = nameEl.dataset.placeholder || 'Pilih gambar...';

      trigger.addEventListener('click', function () { input.click(); });
      trigger.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); input.click(); }
      });

      input.addEventListener('change', function () {
        var file = input.files && input.files[0];
        if (!file) return;
        nameEl.textContent = file.name;
        nameEl.classList.remove('is-placeholder');
        clearBtn.classList.remove('is-hidden');
        removeFlag.value = '0';
      });

      clearBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        input.value = '';
        removeFlag.value = '1';
        nameEl.textContent = placeholder;
        nameEl.classList.add('is-placeholder');
        clearBtn.classList.add('is-hidden');
      });
    });
  }

  /* Modern Comma-Separated Tag Chip Editor */
  function initTagEditors() {
    document.querySelectorAll('[data-tag-editor]').forEach(function (box) {
      var hidden = box.querySelector('.tag-hidden-csv');
      var field = box.querySelector('.tag-chip-field');
      var form = box.closest('form');
      var tags = [];

      function normalize(str) {
        return str.trim().replace(/\s+/g, ' ');
      }

      function addTagsFromRaw(raw) {
        if (!raw) return;
        raw.split(',').forEach(function (part) {
          var clean = normalize(part);
          if (!clean) return;
          var exists = tags.some(function (t) { return t.toLowerCase() === clean.toLowerCase(); });
          if (!exists) {
            tags.push(clean);
          }
        });
        render();
      }

      function removeTag(index) {
        tags.splice(index, 1);
        render();
      }

      function render() {
        box.querySelectorAll('.tag-chip').forEach(function (c) { c.remove(); });
        tags.forEach(function (tag, idx) {
          var chip = document.createElement('span');
          chip.className = 'tag-chip';
          chip.textContent = tag;
          var btn = document.createElement('button');
          btn.type = 'button';
          btn.className = 'tag-chip-remove';
          btn.setAttribute('aria-label', 'Hapus tag ' + tag);
          btn.textContent = '\u00d7';
          btn.addEventListener('click', function (e) {
            e.stopPropagation();
            removeTag(idx);
          });
          chip.appendChild(btn);
          box.insertBefore(chip, field);
        });
        hidden.value = tags.join(', ');
      }

      if (hidden.value) {
        addTagsFromRaw(hidden.value);
      }

      box.addEventListener('click', function () { field.focus(); });

      field.addEventListener('keydown', function (e) {
        if (e.key === ',' || e.key === 'Enter') {
          e.preventDefault();
          if (field.value.trim()) {
            addTagsFromRaw(field.value);
            field.value = '';
          }
        } else if (e.key === 'Backspace' && field.value === '' && tags.length > 0) {
          removeTag(tags.length - 1);
        }
      });

      field.addEventListener('blur', function () {
        if (field.value.trim()) {
          addTagsFromRaw(field.value);
          field.value = '';
        }
      });

      if (form) {
        form.addEventListener('submit', function () {
          if (field.value.trim()) {
            addTagsFromRaw(field.value);
            field.value = '';
          }
          hidden.value = tags.join(', ');
        });
      }

      var suggestWrap = box.parentNode.querySelector('[data-tag-suggestions]');
      if (suggestWrap) {
        suggestWrap.querySelectorAll('.tag-suggest-btn').forEach(function (btn) {
          btn.addEventListener('click', function () {
            addTagsFromRaw(btn.dataset.tagName);
            field.focus();
          });
        });
      }
    });
  }

  function boot() {
    init();
    initImageUploads();
    initTagEditors();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
}());
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
