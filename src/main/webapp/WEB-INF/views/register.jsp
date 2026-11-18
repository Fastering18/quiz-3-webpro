<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height:70vh;">
  <div class="glass-card p-4 p-md-5 w-100" style="max-width:460px;">
    <h2 class="fw-bold text-center text-white mb-4">Daftar Akun</h2>

    <c:if test="${not empty error}">
      <div class="alert alert-danger small"><c:out value="${error}" /></div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/register">
      <div class="mb-3">
        <label for="name" class="form-label fw-semibold">Nama Lengkap</label>
        <input type="text" id="name" name="name" class="form-control form-dark" required placeholder="Nama lengkap Anda">
      </div>
      <div class="mb-3">
        <label for="email" class="form-label fw-semibold">Email</label>
        <input type="email" id="email" name="email" class="form-control form-dark" required placeholder="nama@email.com">
      </div>
      <div class="mb-4">
        <label for="password" class="form-label fw-semibold">Password</label>
        <input type="password" id="password" name="password" class="form-control form-dark" required minlength="6" placeholder="Minimal 6 karakter">
      </div>
      <button type="submit" class="btn btn-accent w-100 py-2">Daftar Sekarang</button>
    </form>

    <div class="text-center mt-3 small text-muted-soft">
      Sudah punya akun? <a href="${pageContext.request.contextPath}/login" style="color:var(--accent);" class="fw-semibold">Masuk</a>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

