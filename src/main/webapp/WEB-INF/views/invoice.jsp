<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:860px;">

  <%-- Flash banners — dark surface, no bright bootstrap colours --%>
  <c:if test="${param.created == '1'}">
    <div class="inv-alert inv-alert-success">Pesanan berhasil dibuat. Selesaikan pembayaran jika status masih Unpaid.</div>
  </c:if>
  <c:if test="${param.paid == '1'}">
    <div class="inv-alert inv-alert-success">Pembayaran dikonfirmasi. Kunci lisensi sudah aktif di bawah ini.</div>
  </c:if>
  <c:if test="${param.cancelled == '1'}">
    <div class="inv-alert inv-alert-warning">Pesanan dibatalkan dan stok lisensi dikembalikan ke katalog.</div>
  </c:if>
  <c:if test="${not empty param.simulated}">
    <div class="inv-alert inv-alert-info">Simulasi webhook: status diubah ke <strong><c:out value="${param.simulated}" /></strong>.</div>
  </c:if>

  <c:choose>
    <c:when test="${trx == null}">
      <div class="glass-card p-4 p-md-5">
        <h3 class="fw-bold text-white mb-1">Cek Invoice</h3>
        <p class="text-muted-soft small mb-4">Masukkan nomor ID transaksi untuk melihat detail atau kunci lisensi.</p>
        <c:if test="${not empty error}">
          <div class="inv-alert inv-alert-danger"><c:out value="${error}" /></div>
        </c:if>
        <form method="GET" action="${pageContext.request.contextPath}/invoice" class="d-flex gap-2">
          <input type="number" name="id" class="form-control form-dark" placeholder="Contoh: 1" min="1" required>
          <button type="submit" class="inv-btn inv-btn-primary">Cari</button>
        </form>
      </div>
    </c:when>
    <c:otherwise>
      <div class="glass-card p-4 p-md-5" id="invoiceRoot"
           data-trx-id="${trx.idTrx}"
           data-status="${trx.status}">

        <%-- ── Header ── --%>
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 pb-3 mb-4"
             style="border-bottom:1px solid rgba(255,255,255,0.08);">
          <div>
            <h3 class="fw-bold text-white mb-1">Invoice #<c:out value="${trx.idTrx}" /></h3>
            <div class="small" style="color:#94a3b8;">
              Order ID:
              <span class="inv-order-id" style="color:#e2e8f0;">
                <c:out value="${trx.invoice != null ? trx.invoice.externalId : '-'}" />
              </span>
              <c:if test="${trx.invoice != null && not empty trx.invoice.provider}">
                <span style="color:#475569;">&nbsp;&middot;&nbsp;</span>
                <span style="color:#94a3b8;"><c:out value="${trx.invoice.provider}" /></span>
              </c:if>
            </div>
            <c:if test="${trx.invoice != null && not empty trx.invoice.providerPaymentId}">
              <div class="small" style="color:#94a3b8;">
                Payment ID: <span class="inv-order-id" style="color:#e2e8f0;"><c:out value="${trx.invoice.providerPaymentId}" /></span>
              </div>
            </c:if>
          </div>
          <div class="text-md-end">
            <span class="status-badge status-${trx.statusClass}"><c:out value="${trx.status}" /></span>
            <div class="num-tabular" style="font-size:0.8rem; color:#94a3b8; margin-top:6px;">
              <fmt:formatDate value="${trx.tanggalTrx}" pattern="dd MMM yyyy HH:mm" />
            </div>
            <c:if test="${trx.invoice != null && trx.invoice.expiresAt != null && trx.status == 'Unpaid'}">
              <div class="inv-expiry">
                Kedaluwarsa: <strong class="num-tabular"><fmt:formatDate value="${trx.invoice.expiresAt}" pattern="dd MMM yyyy HH:mm" /></strong>
              </div>
            </c:if>
          </div>
        </div>

        <%-- ── QRIS payment link ── --%>
        <c:if test="${trx.status == 'Unpaid' && trx.invoice != null && trx.invoice.externalPaymentLink}">
          <div class="p-3 mb-4 d-flex justify-content-between align-items-center flex-wrap gap-3"
               style="background:rgba(99,102,241,0.07); border:1px solid rgba(99,102,241,0.20); border-radius:10px;">
            <div>
              <div class="fw-semibold text-white" style="font-size:0.875rem;">Pembayaran QRIS Siap</div>
              <div class="small text-muted-soft">Scan QR di halaman Sumopod. Status diperbarui otomatis.</div>
            </div>
            <div class="d-flex gap-2">
              <a href="<c:out value='${trx.invoice.paymentLink}'/>" target="_blank" rel="noopener"
                 class="inv-btn inv-btn-primary">Bayar via QRIS</a>
              <a href="${pageContext.request.contextPath}/invoice?id=${trx.idTrx}"
                 class="inv-btn inv-btn-neutral">Muat Ulang</a>
            </div>
          </div>
        </c:if>

        <%-- ── Detail Pembeli ── --%>
        <div class="mb-4">
          <div class="inv-section-label">Detail Pembeli</div>
          <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid rgba(255,255,255,0.07);">
            <span class="small text-muted-soft">Nama Penerima</span>
            <span class="small"><c:out value="${not empty trx.namaPenerima ? trx.namaPenerima : (not empty trx.namaPelanggan ? trx.namaPelanggan : 'Guest')}" /></span>
          </div>
          <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid rgba(255,255,255,0.07);">
            <span class="small text-muted-soft">Email</span>
            <span class="small"><c:out value="${trx.gmailTrx}" /></span>
          </div>
          <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid rgba(255,255,255,0.07);">
            <span class="small text-muted-soft">Metode Pembayaran</span>
            <span class="small"><c:out value="${trx.metodePembayaran}" /></span>
          </div>
        </div>

        <%-- ── Item & Kunci Lisensi ── --%>
        <div class="mb-4">
          <div class="inv-section-label">Item &amp; Kunci Lisensi</div>
          <c:forEach var="d" items="${trx.details}">
            <div class="p-3 rounded-3 mb-2" style="background:rgba(10,8,18,0.70); border:1px solid rgba(255,255,255,0.07);">
              <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div>
                  <div class="fw-semibold text-white small"><c:out value="${d.namaProduk}" /></div>
                  <div class="small text-muted-soft">Stock #<c:out value="${d.idStk}" /></div>
                </div>
                <div class="fw-semibold small num-tabular">
                  Rp <fmt:formatNumber value="${d.hargaBeli}" groupingUsed="true" />
                </div>
              </div>
              <div class="mt-2">
                <c:choose>
                  <c:when test="${trx.status == 'Completed'}">
                    <div class="d-flex align-items-center gap-2 flex-wrap mt-1">
                      <span class="license-box"><c:out value="${d.kodeLisensi}" /></span>
                      <button type="button" class="btn btn-xs btn-tbl-ghost btn-copy-license no-print"
                              data-key="<c:out value='${d.kodeLisensi}'/>">Salin</button>
                    </div>
                  </c:when>
                  <c:when test="${trx.status == 'Failed' || trx.status == 'Expired' || trx.status == 'Cancelled'}">
                    <span class="small text-danger">
                      Pesanan <c:out value="${trx.status}" />. Stok dikembalikan ke katalog.
                    </span>
                  </c:when>
                  <c:otherwise>
                    <div class="inv-license-pending">
                      <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.75"
                           stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <rect x="5" y="9" width="10" height="8" rx="1.5"/>
                        <path d="M7 9V6.5a3 3 0 016 0V9"/>
                      </svg>
                      Kunci lisensi tampil setelah pembayaran lunas.
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>

        <%-- ── Biaya gateway ── --%>
        <c:if test="${trx.invoice != null && trx.invoice.fee > 0}">
          <div class="d-flex justify-content-between py-2 small" style="border-top:1px solid rgba(255,255,255,0.07);">
            <span class="text-muted-soft">Subtotal Produk</span>
            <span class="num-tabular">Rp <fmt:formatNumber value="${trx.invoice.netAmount}" groupingUsed="true" /></span>
          </div>
          <div class="d-flex justify-content-between py-2 small">
            <span class="text-muted-soft">Biaya Layanan</span>
            <span class="num-tabular">Rp <fmt:formatNumber value="${trx.invoice.fee}" groupingUsed="true" /></span>
          </div>
        </c:if>

        <%-- ── Total ── --%>
        <div class="d-flex justify-content-between align-items-center py-3 mb-4"
             style="border-top:1px solid rgba(255,255,255,0.09); border-bottom:1px solid rgba(255,255,255,0.09);">
          <span class="fw-semibold text-white">Total Pembayaran</span>
          <span class="fs-5 fw-bold text-white num-tabular">
            Rp <fmt:formatNumber value="${trx.totalPembayaran}" groupingUsed="true" />
          </span>
        </div>

        <%-- ── Action buttons ── --%>
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
          <%-- Left: transaction actions --%>
          <div class="d-flex gap-2 flex-wrap">
            <c:if test="${trx.status == 'Unpaid'}">
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="confirm">
                <button type="submit" class="inv-btn inv-btn-primary">Konfirmasi Pembayaran</button>
              </form>
              <%-- hidden cancel form submitted by modal --%>
              <form id="inv-cancel-form" method="POST" action="${pageContext.request.contextPath}/invoice" style="display:none;">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="cancel">
              </form>
              <button type="button" class="inv-btn inv-btn-danger"
                      onclick="openInvCancelModal(${trx.idTrx})">Batalkan Pesanan</button>
            </c:if>
          </div>
          <%-- Right: utility actions --%>
          <div class="d-flex gap-2 no-print">
            <button type="button" class="inv-btn inv-btn-neutral no-print" onclick="window.print()">
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.75"
                   stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M5 7V3h10v4"/>
                <rect x="3" y="7" width="14" height="8" rx="1.5"/>
                <path d="M7 15v2h6v-2"/>
              </svg>
              Cetak Invoice
            </button>
            <a href="${pageContext.request.contextPath}/home" class="inv-btn inv-btn-ghost no-print">
              Kembali ke Katalog
            </a>
          </div>
        </div>

        <%-- ── Sandbox Simulator (debug only) ── --%>
        <c:if test="${debugMode && trx.status == 'Unpaid'}">
          <div class="inv-sandbox no-print">
            <div class="inv-sandbox-title">Mode Sandbox &middot; Simulasi Webhook</div>
            <div class="inv-sandbox-desc">
              Uji perubahan status pembayaran lokal untuk Order ID
              <span class="inv-order-id"><c:out value="${trx.invoice != null ? trx.invoice.externalId : '-'}" /></span>.
            </div>
            <div class="d-flex gap-2 flex-wrap">
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_paid">
                <button type="submit" class="inv-sim-btn inv-sim-paid">Simulasikan PAID</button>
              </form>
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_failed">
                <button type="submit" class="inv-sim-btn inv-sim-failed">Simulasikan FAILED</button>
              </form>
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_expired">
                <button type="submit" class="inv-sim-btn inv-sim-expired">Simulasikan EXPIRED</button>
              </form>
            </div>
          </div>
        </c:if>

      </div><%-- /invoiceRoot --%>
    </c:otherwise>
  </c:choose>
</div>

<%-- ── Cancel confirmation modal ── --%>
<div id="invCancelModal" class="inv-cancel-modal-overlay" role="dialog" aria-modal="true">
  <div class="inv-cancel-modal-box">
    <h5 class="fw-semibold text-white mb-1">Batalkan Transaksi?</h5>
    <p class="text-muted-soft small mb-0">
      Transaksi <strong id="invCancelTrxId" class="text-white"></strong> akan dibatalkan
      dan stok yang dipesan dikembalikan ke katalog.
    </p>
    <div class="inv-cancel-modal-actions">
      <button type="button" class="inv-btn inv-btn-ghost" onclick="closeInvCancelModal()">Kembali</button>
      <button type="button" class="inv-btn inv-btn-danger fw-semibold" id="invCancelConfirm">Ya, Batalkan</button>
    </div>
  </div>
</div>

<script>
  /* ── Cancel modal ── */
  (function () {
    var modal   = document.getElementById('invCancelModal');
    var confirm = document.getElementById('invCancelConfirm');
    if (!modal || !confirm) return;

    window.openInvCancelModal = function (trxId) {
      document.getElementById('invCancelTrxId').textContent = '#' + trxId;
      modal.classList.add('is-open');
      document.body.style.overflow = 'hidden';
    };

    window.closeInvCancelModal = function () {
      modal.classList.remove('is-open');
      document.body.style.overflow = '';
    };

    confirm.addEventListener('click', function () {
      var form = document.getElementById('inv-cancel-form');
      if (form) form.submit();
    });

    modal.addEventListener('click', function (e) {
      if (e.target === modal) closeInvCancelModal();
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && modal.classList.contains('is-open')) closeInvCancelModal();
    });
  }());

  /* ── Copy-to-clipboard for license keys ── */
  document.querySelectorAll('.btn-copy-license').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var key = btn.dataset.key;
      navigator.clipboard.writeText(key).then(function () {
        btn.textContent = 'Tersalin';
        setTimeout(function () { btn.textContent = 'Salin'; }, 2000);
      }).catch(function () {
        btn.textContent = 'Gagal';
        setTimeout(function () { btn.textContent = 'Salin'; }, 2000);
      });
    });
  });

  /* ── Auto-poll for Unpaid status ── */
  (function () {
    var root = document.getElementById('invoiceRoot');
    if (!root || root.dataset.status !== 'Unpaid') return;
    var trxId   = root.dataset.trxId;
    var pollUrl = '${pageContext.request.contextPath}/invoice?id=' + encodeURIComponent(trxId) + '&format=json';
    var timer = setInterval(async function () {
      try {
        var res = await fetch(pollUrl, { headers: { 'Accept': 'application/json' } });
        if (!res.ok) return;
        var data = await res.json();
        if (data.status && data.status !== 'Unpaid') {
          clearInterval(timer);
          window.location.reload();
        }
      } catch (e) { /* ignore transient errors */ }
    }, 5000);
  }());
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
