<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1100px;">
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
    <div>
      <h2 class="fw-bold text-white mb-1">Dashboard Pelanggan</h2>
      <p class="text-muted-soft mb-0">
        Selamat datang, <strong class="text-white"><c:out value="${sessionScope.user.namaPelanggan}" /></strong>
        (<c:out value="${sessionScope.user.gmailPelanggan}" />)
      </p>
    </div>
    <c:if test="${sessionScope.user.admin}">
      <a href="${pageContext.request.contextPath}/admin" class="btn btn-accent">Buka Admin Panel</a>
    </c:if>
  </div>

  <c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success"><c:out value="${sessionScope.flashSuccess}" /></div>
    <c:remove var="flashSuccess" scope="session" />
  </c:if>
  <c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger"><c:out value="${sessionScope.flashError}" /></div>
    <c:remove var="flashError" scope="session" />
  </c:if>

  <!-- Ringkasan Akun -->
  <div class="row g-3 mb-4">
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="small text-muted-soft">Level Keanggotaan</div>
        <div class="fs-4 fw-bold text-white mt-1"><c:out value="${sessionScope.user.namaLevel}" /></div>
        <div class="small" style="color:var(--accent);">Diskon pembelian: <c:out value="${sessionScope.user.diskonLevel}" />%</div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="small text-muted-soft">Saldo Akun</div>
        <div class="fs-4 fw-bold text-white mt-1">
          Rp <fmt:formatNumber value="${sessionScope.user.saldo}" groupingUsed="true" />
        </div>
        <div class="small text-muted-soft">Dapat digunakan untuk checkout instan</div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="small text-muted-soft">Total Pembelian</div>
        <div class="fs-4 fw-bold text-white mt-1">
          Rp <fmt:formatNumber value="${sessionScope.user.totalPembelian}" groupingUsed="true" />
        </div>
        <div class="small text-muted-soft"><c:out value="${transactions.size()}" /> transaksi tercatat</div>
      </div>
    </div>
  </div>

  <!-- Form Edit Profil & Top-Up Saldo -->
  <div class="row g-4 mb-5">
    <div class="col-lg-6">
      <div class="glass-card p-4 h-100">
        <h5 class="fw-bold text-white mb-3">Perbarui Profil Akun</h5>
        <form method="POST" action="${pageContext.request.contextPath}/dashboard">
          <input type="hidden" name="action" value="update_profile">
          <div class="mb-3">
            <label class="form-label fw-semibold">Nama Lengkap</label>
            <input type="text" name="name" class="form-control form-dark"
                   value="<c:out value='${sessionScope.user.namaPelanggan}'/>" required>
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">Password Baru (Opsional)</label>
            <input type="password" name="password" class="form-control form-dark"
                   placeholder="Kosongkan jika tidak ingin mengganti password">
          </div>
          <button type="submit" class="btn btn-accent">Simpan Perubahan</button>
        </form>
      </div>
    </div>

    <div class="col-lg-6">
      <div class="glass-card p-4 h-100">
        <h5 class="fw-bold text-white mb-3">Top-Up Saldo Akun</h5>
        <form method="POST" action="${pageContext.request.contextPath}/dashboard">
          <input type="hidden" name="action" value="topup">
          <div class="mb-3">
            <label class="form-label fw-semibold">Nominal Top-Up (Rp)</label>
            <input type="number" name="amount" class="form-control form-dark" min="10000" max="50000000"
                   step="10000" value="500000" required>
            <div class="form-text text-muted-soft">Minimal Rp 10.000, maksimal Rp 50.000.000 per pengisian.</div>
          </div>
          <button type="submit" class="btn btn-accent">Tambah Saldo</button>
        </form>
      </div>
    </div>
  </div>

  <!-- Riwayat Transaksi -->
  <h4 class="fw-bold text-white section-heading mb-3">Riwayat Transaksi</h4>
  <c:choose>
    <c:when test="${not empty transactions}">
      <div class="d-grid gap-3">
        <c:forEach var="t" items="${transactions}">
          <div class="glass-card p-4">
            <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
              <div>
                <div class="fw-bold" style="color:var(--accent);">Transaksi #<c:out value="${t.idTrx}" /></div>
                <div class="small text-muted-soft">
                  <fmt:formatDate value="${t.tanggalTrx}" pattern="dd MMM yyyy HH:mm" />
                  | Metode: <c:out value="${t.metodePembayaran}" />
                </div>
              </div>
              <span class="status-badge status-${t.statusClass}"><c:out value="${t.status}" /></span>
            </div>

            <div class="fs-4 fw-bold text-white my-2">
              Rp <fmt:formatNumber value="${t.totalPembayaran}" groupingUsed="true" />
            </div>

            <div class="d-flex gap-2 flex-wrap mt-2">
              <a href="${pageContext.request.contextPath}/invoice?id=${t.idTrx}" class="btn btn-sm btn-accent">
                Lihat Invoice &amp; Lisensi
              </a>
              <c:if test="${t.status == 'Unpaid'}">
                <form method="POST" action="${pageContext.request.contextPath}/dashboard"
                      onsubmit="return confirm('Batalkan transaksi ini?');">
                  <input type="hidden" name="action" value="cancel_order">
                  <input type="hidden" name="id_trx" value="${t.idTrx}">
                  <button type="submit" class="btn btn-sm btn-outline-danger">Batalkan</button>
                </form>
              </c:if>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:when>
    <c:otherwise>
      <div class="glass-card p-4 text-center text-muted-soft">
        Belum ada riwayat transaksi. <a href="${pageContext.request.contextPath}/home" style="color:var(--accent);">Belanja sekarang</a>.
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

