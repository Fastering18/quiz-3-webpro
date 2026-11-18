<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height:70vh;">
  <div class="glass-card p-4 p-md-5 w-100" style="max-width:440px;">
    <h2 class="fw-bold text-center text-white mb-4">Login Akun</h2>

    <c:if test="${param.registered == '1'}">
      <div class="alert alert-success small">Registrasi berhasil. Silakan login menggunakan akun Anda.</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger small"><c:out value="${error}" /></div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/login">
      <div class="mb-3">
        <label for="email" class="form-label fw-semibold">Email</label>
        <input type="email" id="email" name="email" class="form-control form-dark" required placeholder="nama@email.com">
      </div>
      <div class="mb-4">
        <label for="password" class="form-label fw-semibold">Password</label>
        <input type="password" id="password" name="password" class="form-control form-dark" required placeholder="Minimal 6 karakter">
      </div>
      <button type="submit" class="btn btn-accent w-100 py-2">Masuk</button>
    </form>

    <div class="text-center mt-3 small text-muted-soft">
      Belum punya akun? <a href="${pageContext.request.contextPath}/register" style="color:var(--accent);" class="fw-semibold">Daftar di sini</a>
    </div>

    <div class="mt-4 p-3 rounded-3 small" style="background:rgba(168,85,247,0.08); border:1px solid var(--border);">
      <div class="fw-semibold text-white mb-1">Akun Uji Coba</div>
      <div class="text-muted-soft">Admin: <code>admin@zstore.id</code> / <code>admin123</code></div>
      <div class="text-muted-soft">Pelanggan: <code>budi@zstore.id</code> / <code>customer123</code></div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

