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
          <form method="POST" action="${pageContext.request.contextPath}/admin">
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
              <label class="form-label small fw-semibold">Path Thumbnail</label>
              <input type="text" name="thumbnail_img" class="form-control form-dark"
                     value="<c:out value='${editingProduct != null ? editingProduct.thumbnailImg : "/static/Image/Logo/Logo.png"}'/>">
            </div>

            <div class="mb-2">
              <label class="form-label small fw-semibold">Deskripsi</label>
              <textarea name="deskripsi" rows="3" class="form-control form-dark"><c:out value="${editingProduct != null ? editingProduct.deskripsi : ''}" /></textarea>
            </div>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Tags</label>
              <div class="d-flex flex-wrap gap-2">
                <c:forEach var="t" items="${tags}">
                  <c:set var="isChecked" value="false" />
                  <c:if test="${editingProduct != null}">
                    <c:forEach var="et" items="${editingProduct.tags}">
                      <c:if test="${et.idTag == t.idTag}">
                        <c:set var="isChecked" value="true" />
                      </c:if>
                    </c:forEach>
                  </c:if>
                  <label class="small d-flex align-items-center gap-1">
                    <input type="checkbox" name="tag_ids" value="${t.idTag}" ${isChecked ? 'checked' : ''}>
                    <span><c:out value="${t.namaTag}" /></span>
                  </label>
                </c:forEach>
              </div>
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
          <div class="table-responsive" style="max-height:620px;">
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
                    <td class="fw-semibold"><c:out value="${p.namaProduk}" /></td>
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
          <form method="POST" action="${pageContext.request.contextPath}/admin">
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
              <label class="form-label small fw-semibold">Path Thumbnail</label>
              <input type="text" name="thumbnail_img" class="form-control form-dark"
                     value="<c:out value='${editingProvider != null ? editingProvider.thumbnailImg : "/static/Image/Logo/Logo.png"}'/>">
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
          <div class="table-responsive">
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
                    <td class="fw-semibold"><c:out value="${pr.nama}" /></td>
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
          <div class="table-responsive" style="max-height:600px;">
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
          <h5 class="fw-bold text-white mb-3">Tambah Tag Baru</h5>
          <form method="POST" action="${pageContext.request.contextPath}/admin">
            <input type="hidden" name="tab" value="tags">
            <input type="hidden" name="action" value="tag_create">
            <div class="mb-3">
              <label class="form-label small fw-semibold">Nama Tag</label>
              <input type="text" name="nama_tag" class="form-control form-dark" placeholder="Misal: Enterprise" required>
            </div>
            <button type="submit" class="btn btn-accent w-100">Tambah Tag</button>
          </form>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="glass-card p-4">
          <h5 class="fw-bold text-white mb-3">Daftar Tag</h5>
          <div class="table-responsive">
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
                    <td class="text-end">
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
      <div class="table-responsive">
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
                      <button type="submit" class="btn btn-sm btn-accent w-100">Simpan</button>
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
      <div class="table-responsive">
        <table class="table table-dark-custom table-hover align-middle small">
          <thead>
            <tr>
              <th>ID</th>
              <th>Pembeli</th>
              <th>Total</th>
              <th>Metode</th>
              <th>Status</th>
              <th class="text-end">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${transactions}">
              <tr>
                <td>#<c:out value="${t.idTrx}" /></td>
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
                    <select name="status" class="form-select form-select-sm form-dark" style="width:130px;">
                      <option value="Unpaid" ${t.status == 'Unpaid' ? 'selected' : ''}>Unpaid</option>
                      <option value="Completed" ${t.status == 'Completed' ? 'selected' : ''}>Completed</option>
                      <option value="Failed" ${t.status == 'Failed' ? 'selected' : ''}>Failed</option>
                      <option value="Expired" ${t.status == 'Expired' ? 'selected' : ''}>Expired</option>
                      <option value="Cancelled" ${t.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button type="submit" class="btn btn-sm btn-accent">Ubah</button>
                  </form>
                </td>
                <td class="text-end text-nowrap">
                  <a href="${pageContext.request.contextPath}/invoice?id=${t.idTrx}" class="btn btn-sm btn-ghost">Invoice</a>
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

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

