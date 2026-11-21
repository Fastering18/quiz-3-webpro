<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1100px;">

  <!-- Page header -->
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
    <div>
      <h2 class="fw-bold text-white mb-1">Dashboard</h2>
      <p class="text-muted-soft mb-0 small">
        <c:out value="${sessionScope.user.namaPelanggan}" />
        &nbsp;&middot;&nbsp;
        <c:out value="${sessionScope.user.gmailPelanggan}" />
      </p>
    </div>
    <c:if test="${sessionScope.user.admin}">
      <a href="${pageContext.request.contextPath}/admin" class="btn btn-sm btn-ghost">Admin Panel</a>
    </c:if>
  </div>

  <!-- Flash messages -->
  <c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success mb-4"><c:out value="${sessionScope.flashSuccess}" /></div>
    <c:remove var="flashSuccess" scope="session" />
  </c:if>
  <c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger mb-4"><c:out value="${sessionScope.flashError}" /></div>
    <c:remove var="flashError" scope="session" />
  </c:if>

  <!-- Stat cards -->
  <div class="row g-3 mb-4">
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="stat-label">Level</div>
        <div class="stat-value"><c:out value="${sessionScope.user.namaLevel}" /></div>
        <div class="stat-sub">Diskon <c:out value="${sessionScope.user.diskonLevel}" />% semua produk</div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="stat-label">Saldo</div>
        <div class="stat-value">Rp <fmt:formatNumber value="${sessionScope.user.saldo}" groupingUsed="true" /></div>
        <div class="stat-sub">Saldo aktif</div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="glass-card p-3 h-100">
        <div class="stat-label">Total Belanja</div>
        <div class="stat-value">Rp <fmt:formatNumber value="${sessionScope.user.totalPembelian}" groupingUsed="true" /></div>
        <div class="stat-sub"><c:out value="${transactions.size()}" /> pesanan</div>
      </div>
    </div>
  </div>

  <!-- Edit Profil & Top-Up -->
  <div class="row g-4 mb-5">
    <div class="col-lg-6">
      <div class="glass-card p-4 h-100">
        <h6 class="fw-semibold text-white mb-3">Profil Akun</h6>
        <form method="POST" action="${pageContext.request.contextPath}/dashboard">
          <input type="hidden" name="action" value="update_profile">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Nama Lengkap</label>
            <input type="text" name="name" class="form-control form-dark"
                   value="<c:out value='${sessionScope.user.namaPelanggan}'/>" required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Password Baru</label>
            <input type="password" name="password" class="form-control form-dark"
                   placeholder="Isi hanya jika ingin mengubah password">
          </div>
          <button type="submit" class="btn btn-dash-primary">Simpan Perubahan</button>
        </form>
      </div>
    </div>

    <div class="col-lg-6">
      <div class="glass-card p-4 h-100">
        <h6 class="fw-semibold text-white mb-3">Top-Up Saldo</h6>
        <form method="POST" action="${pageContext.request.contextPath}/dashboard">
          <input type="hidden" name="action" value="topup">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Nominal (Rp)</label>
            <input type="number" name="amount" class="form-control form-dark"
                   min="10000" max="50000000" step="10000" value="500000" required>
            <div class="form-text text-muted-soft" style="font-size:0.75rem;">
              Min. Rp 10.000 &middot; Maks. Rp 50.000.000
            </div>
          </div>
          <button type="submit" class="btn btn-dash-primary">Tambah Saldo</button>
        </form>
      </div>
    </div>
  </div>

  <!-- Riwayat Transaksi -->
  <h4 class="fw-bold text-white section-heading mb-3">Riwayat Transaksi</h4>

  <c:choose>
    <c:when test="${not empty transactions}">
      <div class="glass-card trx-table-wrap">
        <table class="table table-dark-custom mb-0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tanggal</th>
              <th>Metode</th>
              <th>Total</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${transactions}">
              <tr>
                <td class="trx-id">#<c:out value="${t.idTrx}" /></td>
                <td class="trx-date">
                  <fmt:formatDate value="${t.tanggalTrx}" pattern="dd MMM yyyy" /><br>
                  <span class="trx-time"><fmt:formatDate value="${t.tanggalTrx}" pattern="HH:mm" /></span>
                </td>
                <td class="trx-method"><c:out value="${t.metodePembayaran}" /></td>
                <td class="trx-amount">Rp <fmt:formatNumber value="${t.totalPembayaran}" groupingUsed="true" /></td>
                <td><span class="status-badge status-${t.statusClass}"><c:out value="${t.status}" /></span></td>
                <td class="trx-actions">
                  <a href="${pageContext.request.contextPath}/invoice?id=${t.idTrx}"
                     class="btn btn-xs btn-tbl-ghost">Invoice</a>
                  <c:if test="${t.status == 'Unpaid'}">
                    <!-- hidden cancel form — submitted by modal -->
                    <form id="cancel-form-${t.idTrx}" method="POST"
                          action="${pageContext.request.contextPath}/dashboard" style="display:none;">
                      <input type="hidden" name="action" value="cancel_order">
                      <input type="hidden" name="id_trx" value="${t.idTrx}">
                    </form>
                    <button type="button"
                            class="btn btn-xs btn-tbl-danger"
                            onclick="openCancelModal(${t.idTrx})">Batalkan</button>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:when>
    <c:otherwise>
      <div class="glass-card p-4 text-center text-muted-soft small">
        Belum ada transaksi. <a href="${pageContext.request.contextPath}/home" style="color:var(--accent);">Belanja sekarang</a>.
      </div>
    </c:otherwise>
  </c:choose>

</div>

<!-- Cancel Confirmation Modal -->
<div id="cancelModal" class="dash-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="cancelModalTitle">
  <div class="dash-modal-box">
    <h5 class="fw-semibold text-white mb-1" id="cancelModalTitle">Batalkan Transaksi?</h5>
    <p class="text-muted-soft small mb-0">
      Transaksi <strong id="cancelModalTrxId" class="text-white"></strong> akan dibatalkan
      dan stok yang dipesan dikembalikan ke katalog.
    </p>
    <div class="dash-modal-actions">
      <button type="button" class="btn btn-ghost btn-sm" onclick="closeCancelModal()">Kembali</button>
      <button type="button" class="btn btn-tbl-danger btn-sm fw-semibold" id="cancelModalConfirm">Ya, Batalkan</button>
    </div>
  </div>
</div>

<script>
(function () {
  var modal = document.getElementById('cancelModal');
  var currentTrxId = null;

  window.openCancelModal = function (trxId) {
    currentTrxId = trxId;
    document.getElementById('cancelModalTrxId').textContent = '#' + trxId;
    modal.classList.add('is-open');
    document.body.style.overflow = 'hidden';
  };

  window.closeCancelModal = function () {
    modal.classList.remove('is-open');
    document.body.style.overflow = '';
    currentTrxId = null;
  };

  document.getElementById('cancelModalConfirm').addEventListener('click', function () {
    if (currentTrxId !== null) {
      document.getElementById('cancel-form-' + currentTrxId).submit();
    }
  });

  // Close on backdrop click
  modal.addEventListener('click', function (e) {
    if (e.target === modal) closeCancelModal();
  });

  // Close on Esc
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && modal.classList.contains('is-open')) closeCancelModal();
  });
}());
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
