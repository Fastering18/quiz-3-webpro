<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-5 text-center" style="min-height:65vh; display:flex; flex-direction:column; justify-content:center; align-items:center;">
  <div class="display-1 fw-bold" style="color:var(--accent);">404</div>
  <h2 class="fw-bold text-white mb-2">Halaman Tidak Ditemukan</h2>
  <p class="text-muted-soft mb-4" style="max-width:460px;">
    Maaf, halaman atau produk yang Anda cari tidak tersedia atau telah dipindahkan.
  </p>
  <div class="d-flex gap-2">
    <a href="${pageContext.request.contextPath}/home" class="btn btn-accent px-4">Kembali ke Katalog</a>
    <a href="${pageContext.request.contextPath}/invoice" class="btn btn-ghost px-4">Cek Invoice</a>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

