import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ticket',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="ticket">
      <div class="tear top">
        <svg preserveAspectRatio="none" viewBox="0 0 120 4">
          <polygon points="0,4 5,0 10,4 15,0 20,4 25,0 30,4 35,0 40,4 45,0 50,4 55,0 60,4 65,0 70,4 75,0 80,4 85,0 90,4 95,0 100,4 105,0 110,4 115,0 120,4"></polygon>
        </svg>
      </div>

      <div class="header">
        <div class="logo">
          <svg viewBox="0 0 100 100">
            <rect x="12" y="12" width="76" height="76" rx="16" fill="none" stroke="#000" stroke-width="9"></rect>
            <path d="M36 78 V30 H58 a17 17 0 0 1 0 34 H36" fill="none" stroke="#000" stroke-width="11" stroke-linejoin="round"></path>
          </svg>
        </div>
        <div class="brand">PARKFLOW</div>
        <div class="brand-sub">PARKING EXPRESS CENTRAL</div>
        <div class="muted">NIT: 900.824.192-5 · Régimen Común</div>
        <div class="muted">Cra. 7 #32-16 · PBX: (601) 745-9000</div>
        <div class="muted">Bogotá D.C. · Sede Centro #01</div>
      </div>

      <div class="divider"></div>

      <div class="meta">
        <div class="row big"><span>{{ titulo }}:</span><strong>{{ folio }}</strong></div>
        <div class="row small"><span>TERMINAL:</span><span>TAQUILLA SUR · POS-01</span></div>
        <div class="row small"><span>OPERADOR:</span><span>{{ operador || 'SISTEMA' }}</span></div>
        <div class="row small"><span>FECHA / HORA:</span><strong>{{ fecha | date:'dd/MM/yyyy - HH:mm:ss' }}</strong></div>
      </div>

      <div class="divider"></div>

      <div class="plate-box">
        <span class="plate-label">PLACA IDENTIFICADA</span>
        <span class="plate-value mono">{{ placa || '---' }}</span>
        <div class="plate-sub">
          <strong>{{ categoria === 'MOTO' ? 'MOTOCICLETA' : 'AUTOMÓVIL' }}</strong>
          <span>·</span>
          <span>{{ novedad || 'Sin novedad' }}</span>
        </div>
      </div>

      <div class="row"><span class="bold">ESPACIO ASIGNADO:</span><span class="bay mono">{{ espacio || '---' }}</span></div>
      <div class="row small"><span>TARIFA BASE:</span><strong>{{ (tarifaValor || 0) | currency:'COP':'symbol-narrow':'1.0-0' }} / hora o fracción</strong></div>
      <div class="row small"><span>MODALIDAD:</span><strong>{{ modalidad || 'POR HORAS' }}</strong></div>

      <div class="divider"></div>

      <div class="barcode">
        <span *ngFor="let w of barras"
              [class.black]="w.black" [class.white]="!w.black"
              [style.width.px]="w.w"></span>
      </div>
      <div class="barcode-text mono">{{ folio.replace('-', '') }}-{{ (placa || 'KLR892').replace('-', '') }}</div>

      <div class="qr">
        <svg viewBox="0 0 100 100">
          <rect fill="black" height="28" width="28" x="0" y="0"></rect>
          <rect fill="white" height="20" width="20" x="4" y="4"></rect>
          <rect fill="black" height="12" width="12" x="8" y="8"></rect>
          <rect fill="black" height="28" width="28" x="72" y="0"></rect>
          <rect fill="white" height="20" width="20" x="76" y="4"></rect>
          <rect fill="black" height="12" width="12" x="80" y="8"></rect>
          <rect fill="black" height="28" width="28" x="0" y="72"></rect>
          <rect fill="white" height="20" width="20" x="4" y="76"></rect>
          <rect fill="black" height="12" width="12" x="8" y="80"></rect>
          <rect height="4" width="4" x="34" y="8"></rect>
          <rect height="4" width="4" x="42" y="8"></rect>
          <rect height="4" width="4" x="50" y="8"></rect>
          <rect height="4" width="4" x="58" y="8"></rect>
          <rect height="4" width="4" x="8" y="34"></rect>
          <rect height="4" width="4" x="8" y="42"></rect>
          <rect height="4" width="4" x="8" y="50"></rect>
          <rect height="4" width="4" x="8" y="58"></rect>
          <rect height="8" width="8" x="36" y="36"></rect>
          <rect height="8" width="8" x="52" y="36"></rect>
          <rect height="8" width="16" x="40" y="52"></rect>
          <rect height="16" width="8" x="64" y="52"></rect>
          <rect height="8" width="8" x="36" y="72"></rect>
          <rect height="6" width="12" x="52" y="72"></rect>
          <rect height="12" width="12" x="76" y="76"></rect>
          <rect height="12" width="4" x="88" y="44"></rect>
          <rect height="8" width="8" x="48" y="84"></rect>
        </svg>
      </div>

      <div class="legal">
        <p class="bold">CONSERVE ESTE TICKET HASTA LA SALIDA</p>
        <p>La pérdida de este comprobante genera un cobro administrativo de $10.000 COP y verificación obligatoria de tarjeta de propiedad.</p>
        <p>La administración no se responsabiliza por objetos de valor no declarados en portería conforme a la Ley 1480 de 2011.</p>
        <p class="mono" style="margin-top:6px">SISTEMA PARKFLOW · SOFTWARE CERTIFICADO</p>
      </div>

      <div class="tear bottom">
        <svg preserveAspectRatio="none" viewBox="0 0 120 4">
          <polygon points="0,4 5,0 10,4 15,0 20,4 25,0 30,4 35,0 40,4 45,0 50,4 55,0 60,4 65,0 70,4 75,0 80,4 85,0 90,4 95,0 100,4 105,0 110,4 115,0 120,4"></polygon>
        </svg>
      </div>
    </div>
  `,
  styles: [`
    .ticket {
      width: 100%;
      max-width: 360px;
      background: #ffffff;
      color: #000;
      padding: 20px;
      font-family: var(--pf-mono);
      font-size: 12px;
      box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
      display: flex;
      flex-direction: column;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }

    .tear { color: #e2e8f0; height: 8px; overflow: hidden; }
    .tear svg { width: 100%; height: 8px; fill: currentColor; }
    .tear.bottom svg { transform: rotate(180deg); }

    .header { text-align: center; margin: 8px 0; }
    .logo { width: 40px; height: 40px; margin: 0 auto 4px; }
    .logo svg { width: 40px; height: 40px; }
    .brand { font-family: var(--pf-font); font-size: 20px; font-weight: 900; letter-spacing: -0.02em; }
    .brand-sub { font-size: 12px; font-weight: 700; letter-spacing: 0.08em; margin-top: 2px; }
    .muted { color: #4b5563; font-size: 10px; }
    .mono { font-family: var(--pf-mono); }

    .divider { border-top: 1px dashed #000; margin: 8px 0; }

    .meta { display: flex; flex-direction: column; gap: 3px; }
    .row { display: flex; justify-content: space-between; align-items: center; padding: 2px 0; }
    .row.big { font-size: 13px; }
    .row.small { font-size: 11px; }
    .bold { font-weight: 700; }

    .plate-box {
      background: #f1f5f9;
      border-radius: 4px;
      padding: 10px 8px;
      text-align: center;
      margin: 6px 0;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .plate-label { font-size: 10px; text-transform: uppercase; letter-spacing: 0.1em; color: #4b5563; font-weight: 600; }
    .plate-value { font-size: 26px; font-weight: 900; letter-spacing: 0.14em; margin: 2px 0; }
    .plate-sub { display: flex; gap: 8px; font-size: 11px; }

    .bay {
      background: #000; color: #fff; font-weight: 900;
      padding: 1px 8px; font-size: 13px; letter-spacing: 0.05em;
    }

    .barcode {
      display: flex; align-items: stretch; justify-content: center;
      height: 46px; gap: 2px; margin: 10px 0 4px; padding: 0 10px;
    }
    .barcode span { display: block; height: 100%; }
    .barcode span.black { background: #000; }
    .barcode span.white { background: #fff; }

    .barcode-text { text-align: center; font-size: 11px; font-weight: 700; letter-spacing: 0.1em; margin-bottom: 8px; }

    .qr { display: flex; justify-content: center; margin: 6px 0; }
    .qr svg { width: 92px; height: 92px; }

    .legal { text-align: center; font-size: 9px; line-height: 1.35; color: #374151; margin-top: 8px; }
    .legal .bold { font-size: 9px; }
    .legal .mono { font-size: 9px; }
  `]
})
export class TicketComponent {
  @Input() titulo = 'TICKET INGRESO';
  @Input() folio = '---';
  @Input() placa = '---';
  @Input() categoria = 'CARRO';
  @Input() espacio = '---';
  @Input() tarifaValor: number | null = null;
  @Input() modalidad = '';
  @Input() operador = '';
  @Input() novedad = '';
  @Input() fecha: string | Date | null = null;

  // Patrón de barras simulado (ancho en px, negro o blanco)
  barras: { w: number; black: boolean }[] = [
    { w: 3, black: true }, { w: 1, black: false }, { w: 2, black: true }, { w: 4, black: true },
    { w: 1, black: false }, { w: 2, black: true }, { w: 1, black: false }, { w: 3, black: true },
    { w: 2, black: true }, { w: 1, black: false }, { w: 4, black: true }, { w: 2, black: false },
    { w: 1, black: true }, { w: 3, black: true }, { w: 1, black: false }, { w: 2, black: true },
    { w: 1, black: false }, { w: 4, black: true }, { w: 2, black: true }, { w: 1, black: false },
    { w: 3, black: true }, { w: 1, black: false }, { w: 2, black: true }, { w: 4, black: true },
    { w: 1, black: false }, { w: 1, black: true }, { w: 3, black: true }, { w: 2, black: false },
    { w: 2, black: true }, { w: 1, black: false }, { w: 4, black: true }, { w: 3, black: true },
    { w: 1, black: false }, { w: 2, black: true }, { w: 2, black: false }, { w: 3, black: true }
  ];
}
