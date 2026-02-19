# 🔐 Como Usar Permissões no Frontend
## ⚠️ IMPORTANTE: Endpoints Corretos
### ❌ **ERRADO** - Não use este endpoint no frontend:
```typescript
// ❌ Isso causa erro 500 se não for ADMIN ou o próprio usuário
GET /api/users/{id}/permissions
```
### ✅ **CORRETO** - Use este endpoint:
```typescript
// ✅ Todo usuário autenticado pode ver suas próprias permissões
GET /api/users/me/permissions
```
---
## 📊 **Endpoints de Permissões**
| Endpoint | Quem Pode Usar | Uso |
|----------|----------------|-----|
| `GET /api/users/me/permissions` | ✅ **TODOS** (usuário autenticado) | **Frontend usa ESTE** para controlar UI |
| `GET /api/users/modules` | Quem tem AUTH+VIEW | Admin usa para listar módulos disponíveis |
| `GET /api/users/{id}/permissions` | ADMIN ou próprio usuário | Admin usa para ver permissões customizadas de alguém |
| `PUT /api/users/{id}/permissions` | Apenas ADMIN | Admin atualiza permissões |
| `POST /api/users/{id}/permissions/reset` | Apenas ADMIN | Admin reseta para role |
---
## 💻 **Código Frontend Correto**
### **1. Buscar Permissões do Usuário Logado (ao fazer login ou carregar app):**
```typescript
// ✅ CORRETO - Use /me/permissions
export const getMyPermissions = async (): Promise<UserPermissions> => {
  const response = await api.get('/api/users/me/permissions');
  return response.data;
};
// Exemplo de uso no AuthContext:
useEffect(() => {
  if (user) {
    getMyPermissions()
      .then(permissions => {
        setUserPermissions(permissions);
      })
      .catch(error => {
        console.error('Erro ao carregar permissões:', error);
      });
  }
}, [user]);
```
**Resposta do `/me/permissions`:**
```json
{
  "userId": 2,
  "username": "operator",
  "role": "OPERATOR",
  "useCustomPermissions": false,
  "permissions": {
    "CUSTOMERS": ["VIEW", "CREATE", "EDIT"],
    "CONTRACTS": ["VIEW", "CREATE", "EDIT"],
    "INVOICES": ["VIEW", "CREATE", "EDIT"],
    "NETWORK": ["VIEW", "CREATE", "EDIT"],
    "DASHBOARD": ["VIEW"],
    "AUTOMATION": ["VIEW"]
  }
}
```
### **2. Controlar Visibilidade da UI:**
```typescript
// Hook customizado para verificar permissões
export const usePermissions = () => {
  const { userPermissions } = useAuth();
  const hasPermission = (module: string, action: string): boolean => {
    return userPermissions?.permissions[module]?.includes(action) ?? false;
  };
  const hasModule = (module: string): boolean => {
    return !!userPermissions?.permissions[module];
  };
  return { hasPermission, hasModule, userPermissions };
};
// Uso no componente:
const CustomerList = () => {
  const { hasPermission, hasModule } = usePermissions();
  // Não mostrar módulo se não tem acesso
  if (!hasModule('CUSTOMERS')) {
    return <Redirect to="/dashboard" />;
  }
  return (
    <div>
      <h1>Clientes</h1>
      {/* Mostrar botão apenas se pode criar */}
      {hasPermission('CUSTOMERS', 'CREATE') && (
        <Button onClick={handleCreate}>Novo Cliente</Button>
      )}
      {/* Mostrar botão editar apenas se pode editar */}
      {hasPermission('CUSTOMERS', 'EDIT') && (
        <Button onClick={handleEdit}>Editar</Button>
      )}
      {/* Mostrar botão deletar apenas se pode deletar */}
      {hasPermission('CUSTOMERS', 'DELETE') && (
        <Button onClick={handleDelete}>Deletar</Button>
      )}
    </div>
  );
};
```
### **3. Controlar Rotas:**
```typescript
// ProtectedRoute.tsx
interface ProtectedRouteProps {
  children: React.ReactNode;
  module: string;
  action?: string;
}
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  module, 
  action = 'VIEW' 
}) => {
  const { hasPermission, hasModule } = usePermissions();
  const navigate = useNavigate();
  useEffect(() => {
    if (!hasModule(module)) {
      toast.error('Você não tem acesso a este módulo');
      navigate('/dashboard');
    } else if (!hasPermission(module, action)) {
      toast.error('Você não tem permissão para esta ação');
      navigate('/dashboard');
    }
  }, [module, action, hasModule, hasPermission, navigate]);
  if (!hasModule(module) || !hasPermission(module, action)) {
    return null;
  }
  return <>{children}</>;
};
// Uso nas rotas:
<Route 
  path="/customers" 
  element={
    <ProtectedRoute module="CUSTOMERS" action="VIEW">
      <CustomerList />
    </ProtectedRoute>
  } 
/>
<Route 
  path="/customers/new" 
  element={
    <ProtectedRoute module="CUSTOMERS" action="CREATE">
      <CustomerForm />
    </ProtectedRoute>
  } 
/>
```
### **4. Menu Dinâmico:**
```typescript
// Sidebar.tsx
const Sidebar = () => {
  const { hasModule } = usePermissions();
  const menuItems = [
    { 
      label: 'Dashboard', 
      path: '/dashboard', 
      module: 'DASHBOARD', 
      icon: <DashboardIcon /> 
    },
    { 
      label: 'Clientes', 
      path: '/customers', 
      module: 'CUSTOMERS', 
      icon: <PeopleIcon /> 
    },
    { 
      label: 'Contratos', 
      path: '/contracts', 
      module: 'CONTRACTS', 
      icon: <DescriptionIcon /> 
    },
    { 
      label: 'Faturas', 
      path: '/invoices', 
      module: 'INVOICES', 
      icon: <ReceiptIcon /> 
    },
    { 
      label: 'Financeiro', 
      path: '/financial', 
      module: 'FINANCIAL', 
      icon: <AttachMoneyIcon /> 
    },
    { 
      label: 'Rede', 
      path: '/network', 
      module: 'NETWORK', 
      icon: <RouterIcon /> 
    },
  ];
  return (
    <aside>
      <nav>
        {menuItems
          .filter(item => hasModule(item.module))
          .map(item => (
            <NavLink key={item.path} to={item.path}>
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
      </nav>
    </aside>
  );
};
```
---
## 🔧 **Admin: Gerenciar Permissões de Outros Usuários**
**Apenas para tela de administração de usuários:**
```typescript
// Apenas ADMIN pode fazer isso
const UserPermissionsForm = ({ userId }: { userId: number }) => {
  const [modules, setModules] = useState<Module[]>([]);
  const [permissions, setPermissions] = useState<UserPermission[]>([]);
  useEffect(() => {
    // Buscar módulos disponíveis
    api.get('/api/users/modules').then(res => setModules(res.data.modules));
    // Buscar permissões customizadas do usuário (se houver)
    api.get(`/api/users/${userId}/permissions`).then(res => {
      setPermissions(res.data);
    });
  }, [userId]);
  const handleSave = async () => {
    await api.put(`/api/users/${userId}/permissions`, permissions);
    toast.success('Permissões atualizadas!');
  };
  // ... resto do componente
};
```
---
## 🚨 **Erro Comum e Solução**
### ❌ **Erro:**
```
GET /api/users/2/permissions → 500 Internal Server Error
```
### ✅ **Solução:**
**Se você está tentando buscar permissões do usuário logado:**
```typescript
// ❌ ERRADO
const perms = await api.get(`/api/users/${userId}/permissions`);
// ✅ CORRETO
const perms = await api.get('/api/users/me/permissions');
```
**Se você é ADMIN e quer ver permissões de outro usuário:**
```typescript
// ✅ Isso funciona apenas para ADMIN
const perms = await api.get(`/api/users/${userId}/permissions`);
```
---
## 📋 **Checklist de Implementação**
- [ ] Buscar permissões com `/me/permissions` ao fazer login
- [ ] Armazenar permissões no contexto/state global
- [ ] Criar hook `usePermissions()` com `hasPermission()` e `hasModule()`
- [ ] Proteger rotas com `ProtectedRoute`
- [ ] Mostrar/esconder botões baseado em permissões
- [ ] Filtrar menu lateral baseado em módulos disponíveis
- [ ] (Admin) Criar tela de gerenciamento de permissões usando `/api/users/{id}/permissions`
---
## 🎯 **Resumo**
| Situação | Endpoint Correto |
|----------|------------------|
| Ver minhas permissões (frontend) | ✅ `GET /api/users/me/permissions` |
| Admin ver permissões de outro usuário | `GET /api/users/{id}/permissions` |
| Admin atualizar permissões | `PUT /api/users/{id}/permissions` |
**⚠️ NUNCA use `/api/users/{id}/permissions` para buscar permissões do próprio usuário. Use `/me/permissions`!**
---
**🎉 Com isso, o frontend terá controle total sobre a UI baseado nas permissões do usuário!**
