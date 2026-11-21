<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1100px;">

  <!-- Page header -->
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
    <div class="d-flex align-items-center gap-3">
      <div class="dash-avatar-lg" id="dashHeaderAvatar">
        <c:choose>
          <c:when test="${sessionScope.user.hasAvatar}">
            <img src="<c:out value='${sessionScope.user.avatarSrc(pageContext.request.contextPath)}'/>"
                 alt="<c:out value='${sessionScope.user.namaPelanggan}'/>">
          </c:when>
          <c:otherwise>
            <c:out value="${sessionScope.user.initial}" />
          </c:otherwise>
        </c:choose>
      </div>
      <div>
        <h2 class="fw-bold text-white mb-1">Dashboard</h2>
        <p class="text-muted-soft mb-0 small">
          <c:out value="${sessionScope.user.namaPelanggan}" />
          &nbsp;&middot;&nbsp;
          <c:out value="${sessionScope.user.gmailPelanggan}" />
        </p>
      </div>
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
        <div class="stat-label">Status Akun</div>
        <div class="stat-value"><c:out value="${sessionScope.user.statusAkun}" /></div>
        <div class="stat-sub">Role: <c:out value="${sessionScope.user.role}" /></div>
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

  <!-- Edit Profil & Foto Profil -->
  <div class="glass-card p-4 mb-5">
    <h6 class="fw-semibold text-white mb-3">Profil Akun</h6>
    <form method="POST" action="${pageContext.request.contextPath}/dashboard" enctype="multipart/form-data">
      <input type="hidden" name="action" value="update_profile">
      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label small fw-semibold">Nama Lengkap</label>
          <input type="text" name="name" class="form-control form-dark"
                 value="<c:out value='${sessionScope.user.namaPelanggan}'/>" required>
        </div>
        <div class="col-md-6">
          <label class="form-label small fw-semibold">Password Baru</label>
          <input type="password" name="password" class="form-control form-dark"
                 placeholder="Isi hanya jika ingin mengubah password">
        </div>
        <div class="col-12">
          <label class="form-label small fw-semibold">Foto Profil</label>
          <div class="d-flex align-items-center gap-3 flex-wrap">
            <div class="dash-avatar-lg" id="avatarFormPreview" data-initial="<c:out value='${sessionScope.user.initial}'/>">
              <c:choose>
                <c:when test="${sessionScope.user.hasAvatar}">
                  <img src="<c:out value='${sessionScope.user.avatarSrc(pageContext.request.contextPath)}'/>"
                       alt="Avatar">
                </c:when>
                <c:otherwise>
                  <c:out value="${sessionScope.user.initial}" />
                </c:otherwise>
              </c:choose>
            </div>
            <div class="flex-grow-1" style="min-width:240px;">
              <div class="img-upload-box" data-img-upload data-preview-target="#avatarFormPreview">
                <input type="file" name="avatar_file" class="img-upload-input" accept="image/png,image/jpeg,image/webp,image/gif">
                <input type="hidden" name="remove_avatar" value="0" class="img-upload-remove-flag">
                <div class="img-upload-trigger" role="button" tabindex="0" title="Pilih foto profil">
                  <span class="img-upload-icon">
                    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <rect x="3" y="4" width="18" height="16" rx="2.5"/>
                      <circle cx="8.5" cy="9.5" r="1.6"/>
                      <path d="M21 16l-5.2-5.2a1.4 1.4 0 00-2 0L6 18.5"/>
                    </svg>
                  </span>
                  <span class="img-upload-name ${sessionScope.user.hasAvatar ? '' : 'is-placeholder'}"
                        data-placeholder="Pilih foto profil (PNG/JPG/WEBP, maks 5MB)...">
                    <c:choose>
                      <c:when test="${sessionScope.user.hasAvatar}"><c:out value="${sessionScope.user.avatarFileName}" /></c:when>
                      <c:otherwise>Pilih foto profil (PNG/JPG/WEBP, maks 5MB)...</c:otherwise>
                    </c:choose>
                  </span>
                </div>
                <button type="button" class="img-upload-clear ${sessionScope.user.hasAvatar ? '' : 'is-hidden'}"
                        title="Hapus foto profil" aria-label="Hapus foto profil">
                  <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    <path d="M10 11v6M14 11v6"/>
                    <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="mt-3">
        <button type="submit" class="btn btn-dash-primary">Simpan Perubahan</button>
      </div>
    </form>
  </div>

  <!-- Riwayat Transaksi -->
  <h4 class="fw-bold text-white section-heading mb-3">Riwayat Transaksi</h4>

  <c:choose>
    <c:when test="${not empty transactions}">
      <div class="glass-card trx-table-wrap">
        <table class="table table-dark-custom mb-0">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Tanggal</th>
              <th>Metode</th>
              <th>Total</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="t" items="${transactions}">
              <c:set var="rowOrderId" value="${t.invoice != null && not empty t.invoice.externalId ? t.invoice.externalId : '#'.concat(t.idTrx)}" />
              <tr>
                <td class="trx-id">
                  <div class="inv-order-id"><c:out value="${rowOrderId}" /></div>
                  <div class="text-muted-soft" style="font-size:0.72rem;">Trx #<c:out value="${t.idTrx}" /></div>
                </td>
                <td class="trx-date">
                  <fmt:formatDate value="${t.tanggalTrx}" pattern="dd MMM yyyy" /><br>
                  <span class="trx-time"><fmt:formatDate value="${t.tanggalTrx}" pattern="HH:mm" /></span>
                </td>
                <td class="trx-method"><c:out value="${t.metodePembayaran}" /></td>
                <td class="trx-amount">Rp <fmt:formatNumber value="${t.totalPembayaran}" groupingUsed="true" /></td>
                <td><span class="status-badge status-${t.statusClass}"><c:out value="${t.status}" /></span></td>
                <td class="trx-actions">
                  <a href="${pageContext.request.contextPath}/invoice?order_id=<c:out value='${t.invoice != null && not empty t.invoice.externalId ? t.invoice.externalId : ""}'/>"
                     class="btn btn-xs btn-tbl-ghost">Invoice</a>
                  <c:if test="${t.status == 'Unpaid'}">
                    <form id="cancel-form-${t.idTrx}" method="POST"
                          action="${pageContext.request.contextPath}/dashboard" style="display:none;">
                      <input type="hidden" name="action" value="cancel_order">
                      <input type="hidden" name="id_trx" value="${t.idTrx}">
                    </form>
                    <button type="button"
                            class="btn btn-xs btn-tbl-danger"
                            data-trx-id="${t.idTrx}"
                            data-order-label="<c:out value='${rowOrderId}'/>"
                            onclick="openCancelModal(this.dataset.trxId, this.dataset.orderLabel)">Batalkan</button>
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
  /* Cancel modal */
  var modal = document.getElementById('cancelModal');
  var currentTrxId = null;

  window.openCancelModal = function (trxId, orderLabel) {
    currentTrxId = trxId;
    document.getElementById('cancelModalTrxId').textContent = orderLabel || ('#' + trxId);
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

  modal.addEventListener('click', function (e) {
    if (e.target === modal) closeCancelModal();
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && modal.classList.contains('is-open')) closeCancelModal();
  });

  /* Avatar upload widget */
  document.querySelectorAll('[data-img-upload]').forEach(function (box) {
    var input = box.querySelector('.img-upload-input');
    var trigger = box.querySelector('.img-upload-trigger');
    var nameEl = box.querySelector('.img-upload-name');
    var clearBtn = box.querySelector('.img-upload-clear');
    var removeFlag = box.querySelector('.img-upload-remove-flag');
    var previewEl = box.dataset.previewTarget ? document.querySelector(box.dataset.previewTarget) : null;
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
      if (previewEl) {
        var reader = new FileReader();
        reader.onload = function (ev) {
          previewEl.innerHTML = '<img src="' + ev.target.result + '" alt="Preview">';
        };
        reader.readAsDataURL(file);
      }
    });

    clearBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      input.value = '';
      removeFlag.value = '1';
      nameEl.textContent = placeholder;
      nameEl.classList.add('is-placeholder');
      clearBtn.classList.add('is-hidden');
      if (previewEl) {
        previewEl.textContent = previewEl.dataset.initial || 'U';
      }
    });
  });
}());
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
